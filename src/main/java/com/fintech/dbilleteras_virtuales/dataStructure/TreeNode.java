package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

import com.fintech.dbilleteras_virtuales.model.User;

public class TreeNode {

    public User user;
    public int points;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(User user) {
        this.user = user;
        this.points = user.getPoints();
        this.left = null;
        this.right = null;
    }
}
