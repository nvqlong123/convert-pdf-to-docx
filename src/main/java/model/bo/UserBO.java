package model.bo;

import model.dao.UserDAO;
import model.bean.*;

public class UserBO {
    private UserDAO userDAO = new UserDAO();
    
    public boolean validateLogin(String username, String password) {
        if (username == null || password == null || 
            username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }
        return userDAO.checkLogin(username, password);
    }
    
    public User getUserInfo(String username) {
        return userDAO.getUserByUsername(username);
    }
    
    public boolean registerNewUser(String username, String password, 
                                   String email, String fullName) {
        // Validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.length() < 6 ||
            email == null || !email.contains("@")) {
            return false;
        }
        
        // Check if username exists
        if (userDAO.getUserByUsername(username) != null) {
            return false;
        }
        
        User user = new User(username, password, email, fullName);
        return userDAO.registerUser(user);
    }
}