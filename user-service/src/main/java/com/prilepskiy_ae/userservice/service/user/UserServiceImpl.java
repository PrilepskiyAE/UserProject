package com.prilepskiy_ae.userservice.service.user;

import com.prilepskiy_ae.userservice.dto.event.OperationType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserEventProducer producer;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest user) {
        logger.info("Creating new user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User creation failed. Email already exists: {}", user.getEmail());
            throw new UserAlreadyExistsException(user.getEmail());
        }

        UserEntity entity = user.toEntity();

        UserEntity savedEntity = userRepository.save(entity);

        logger.info("User successfully created with id: {}", savedEntity.getId());
        producer.sendUserEvent(user.getEmail(), OperationType.CREATED);
        return savedEntity.toResponse();
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

        logger.info("User successfully deleted with id: {}", id);
        producer.sendUserEvent(entity.getEmail(), OperationType.DELETED);
    }

    @Override
    public boolean isEmailExists(String email) {
        logger.debug("Checking if email exists: {}", email);

        boolean exists = userRepository.existsByEmail(email);

        logger.debug("Email {} exists: {}", email, exists);

        return exists;
    }

}