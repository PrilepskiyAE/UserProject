package com.prilepskiy_ae.userservice.service.user;

import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.common.UserEventDto;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.entity.UserEntity;
import com.prilepskiy_ae.userservice.exception.UserAlreadyExistsException;
import com.prilepskiy_ae.userservice.exception.UserNotFoundException;
import com.prilepskiy_ae.userservice.repository.UserRepository;
import com.prilepskiy_ae.userservice.service.userEvent.UserEventProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserEventProducer producer;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User creation failed. Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException(request.getEmail());
        }

        UserEntity entity = request.toEntity();
        UserEntity saved = userRepository.save(entity);
        logger.info("User successfully created with id: {}", saved.getId());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        CompletableFuture<SendResult<String, UserEventDto>> future =
                                producer.sendUserEvent(request.getEmail(), OperationType.CREATED);

                        future.whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.error(
                                        "Failed to send user.created event for email={}. " +
                                                "Event needs manual check or retry.",
                                        request.getEmail(),
                                        ex
                                );
                            } else {
                                logger.debug(
                                        "User.created event sent successfully. " +
                                                "Topic={}, Partition={}, Offset={}",
                                        result.getRecordMetadata().topic(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset()
                                );
                            }
                        });
                    }
                }
        );

        return saved.toResponse();
    }

    @Override
    public UserResponse getUserById(Long id) {
        logger.info("Fetching user by id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });

        logger.info("User found with id: {}", id);

        return entity.toResponse();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();

        logger.info("Fetched {} users", users.size());

        return users;
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        logger.info("Updating user with id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User update failed. User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });

        String newEmail = request.getEmail().trim();

        if (!entity.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            logger.warn("User update failed. Email already exists: {}", newEmail);
            throw new UserAlreadyExistsException(newEmail);
        }

        entity.setName(request.getName().trim());
        entity.setEmail(newEmail);
        entity.setAge(request.getAge());

        UserEntity updatedEntity = userRepository.save(entity);

        logger.info("User successfully updated with id: {}", updatedEntity.getId());

        return updatedEntity.toResponse();
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        logger.info("Deleting user with id: {}", id);

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User deletion failed. User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });

        userRepository.delete(entity);
        logger.info("User successfully deleted with id: {} (pending commit)", id);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        CompletableFuture<SendResult<String, UserEventDto>> future =
                                producer.sendUserEvent(entity.getEmail(), OperationType.DELETED);
                        future.whenComplete((result, ex) -> {
                            if (ex!= null) {
                                logger.error(
                                        "Failed to send user.deleted event for email={}, id={}. " +
                                                "Event needs manual check or retry.",
                                        entity.getEmail(),
                                        id,
                                        ex
                                );
                            } else {
                                logger.debug(
                                        "User.deleted event sent successfully. " +
                                                "Topic={}, Partition={}, Offset={}",
                                        result.getRecordMetadata().topic(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset()
                                );
                            }
                        });
                    }
                }
        );
    }

    @Override
    public boolean isEmailExists(String email) {
        logger.debug("Checking if email exists: {}", email);

        boolean exists = userRepository.existsByEmail(email);

        logger.debug("Email {} exists: {}", email, exists);

        return exists;
    }

}