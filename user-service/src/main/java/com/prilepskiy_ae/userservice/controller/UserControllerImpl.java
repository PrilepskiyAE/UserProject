package com.prilepskiy_ae.userservice.controller;

import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.EntityModel;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public ResponseEntity<EntityModel<UserResponse>> createUser(UserRequest request) {

        UserResponse response = userService.createUser(request);

        EntityModel<UserResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(response.getId()))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));

        URI location = linkTo(methodOn(UserController.class).getUserById(response.getId())).toUri();
        return ResponseEntity.created(location).body(resource);
    }

    @Override
    public ResponseEntity<EntityModel<UserResponse>> getUserById(Long id) {
        UserResponse response = userService.getUserById(id);

        EntityModel<UserResponse> resource = EntityModel.of(response);

        resource.add(linkTo(methodOn(UserController.class).getUserById(id))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, null))
                .withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id))
                .withRel("delete"));

        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        var resources = responses.stream()
                .map(u -> {
                    var r = EntityModel.of(u);
                    r.add(linkTo(methodOn(UserController.class).getUserById(u.getId()))
                            .withRel("self"));
                    return r;
                })
                .collect(Collectors.toList());

        var collection = CollectionModel.of(resources);

        collection.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("self"));


        return ResponseEntity.ok(collection);
    }

    @Override
    public ResponseEntity<EntityModel<UserResponse>>updateUser(Long id, UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        EntityModel<UserResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));

        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
