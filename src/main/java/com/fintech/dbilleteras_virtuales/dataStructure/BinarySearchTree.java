package com.fintech.dbilleteras_virtuales.dataStructure;

import com.fintech.dbilleteras_virtuales.model.User;
import java.util.ArrayList;
import java.util.List;

public class BinarySearchTree {

    private TreeNode root;

    public BinarySearchTree() {
        this.root = null;
    }

    public void insert(User user) {
        root = insertRecursive(root, user);
    }

    private TreeNode insertRecursive(TreeNode node, User user) {
        if (node == null) {
            return new TreeNode(user);
        }
        if (user.getPoints() < node.points) {
            node.left = insertRecursive(node.left, user);
        } else if (user.getPoints() > node.points) {
            node.right = insertRecursive(node.right, user);
        } else {
            node.right = insertRecursive(node.right, user);
        }
        return node;
    }
    public List<User> inorder() {
        List<User> result = new ArrayList<>();
        inorderRecursive(root, result);
        return result;
    }

    private void inorderRecursive(TreeNode node, List<User> result) {
        if (node != null) {
            inorderRecursive(node.left, result);
            result.add(node.user);
            inorderRecursive(node.right, result);
        }
    }

    public List<User> searchByRange(int minPoints, int maxPoints) {
        List<User> result = new ArrayList<>();
        searchRangeRecursive(root, minPoints, maxPoints, result);
        return result;
    }

    private void searchRangeRecursive(TreeNode node, int min, int max, List<User> result) {
        if (node == null) return;

        if (node.points > min) {
            searchRangeRecursive(node.left, min, max, result);
        }
        if (node.points >= min && node.points <= max) {
            result.add(node.user);
        }
        if (node.points < max) {
            searchRangeRecursive(node.right, min, max, result);
        }
    }

    public User getMaximum() {
        if (root == null) return null;
        TreeNode current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.user;
    }
    public List<User> searchByLevel(String level) {
        int min, max;
        switch (level.toUpperCase()) {
            case "BRONZE": min = 0;    max = 500;        break;
            case "SILVER": min = 501;  max = 1000;       break;
            case "GOLD":   min = 1001; max = 5000;       break;
            case "PLATINUM": min = 5001; max = Integer.MAX_VALUE; break;
            default: return new ArrayList<>();
        }
        return searchByRange(min, max);
    }

    public boolean isEmpty() {
        return root == null;
    }
}