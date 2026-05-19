package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.BinarySearchTree;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTreeService {

    private final UserRepository userRepository;

    private BinarySearchTree buildTree() {
        BinarySearchTree tree = new BinarySearchTree();
        List<User> users = userRepository.findAll();
        users.forEach(tree::insert);
        return tree;
    }

    public List<User> getOrderedUsers() {
        return buildTree().inorder();
    }

    public List<User> getUsersByRange(int min, int max) {
        return buildTree().searchByRange(min, max);
    }

    public User getTopUser() {
        return buildTree().getMaximum();
    }

    public List<User> getUsersByLevel(String level) {
        return buildTree().searchByLevel(level);
    }
}