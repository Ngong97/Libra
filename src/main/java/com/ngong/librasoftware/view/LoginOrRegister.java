package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import javafx.stage.Stage;

public class LoginOrRegister {
    private final DatabaseService db = new DatabaseService();
    public void checkAndProceed() {
        // Check if the user table in the database has data
        if (db.checkIfUserTableHasData()) {
            // If the table has data, invoke the Login window
            Stage registerstage=new Stage();
            LoginWindow login = new LoginWindow();
            login.start(registerstage);
        } else {
            // If the table is empty, invoke the Register window
            Stage registerstage=new Stage();
            SignUpWindow registerWindow = new SignUpWindow();
            registerWindow.start(registerstage);
        }
    }


}

