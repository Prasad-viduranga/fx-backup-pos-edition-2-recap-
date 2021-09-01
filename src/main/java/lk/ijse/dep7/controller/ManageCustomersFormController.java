package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFondException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.util.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class ManageCustomersFormController {
    public AnchorPane root;
    public JFXTextField txtCustomerName;
    public JFXTextField txtCustomerId;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public JFXButton btnAddNewCustomer;
    public JFXTextField txtCustomerAddress;
    public TableView<CustomerTM> tblCustomers;
    public CustomerService customerService = new CustomerService();
    public SingleConnectionDataSource singleConnectionDataSource;

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/main-form.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(() -> primaryStage.sizeToScene());
    }

    public void initialize() throws FailedOperationException {
        txtCustomerId.setDisable(true);
        txtCustomerName.setDisable(true);
        txtCustomerAddress.setDisable(true);

        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        txtCustomerId.setEditable(false);
        btnDelete.setDisable(true);
        btnSave.setDisable(true);
        loadAllCustomer();
        btnSave.setDisable(true);
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            if (newValue != null) {
                txtCustomerId.setDisable(false);
                txtCustomerName.setDisable(false);
                txtCustomerAddress.setDisable(false);
                btnSave.setDisable(false);
                btnSave.setText("Update");
                txtCustomerId.setText(newValue.getCustomerId());
                txtCustomerName.setText(newValue.getCustomerName());
                txtCustomerAddress.setText(newValue.getCustomerAddress());
            } else {
                btnSave.setText("Save");
            }
        });

        txtCustomerName.setOnAction(event -> txtCustomerAddress.requestFocus());
        txtCustomerAddress.setOnAction(event -> {
            if (txtCustomerName.getText().isEmpty()) {
                txtCustomerName.requestFocus();
            } else {
                btnSave.fire();
            }
        });

    }

    private void loadAllCustomer() throws FailedOperationException {
        List<CustomerDTO> allCustomer = null;
        try {
            allCustomer = customerService.findAllCustomer();
            for (CustomerDTO customerDTO : allCustomer) {
                tblCustomers.getItems().add(new CustomerTM(customerDTO.getCustomerId(), customerDTO.getCustomerName(), customerDTO.getCustomerAddress()));
            }
        } catch (FailedOperationException e) {
            throw new FailedOperationException("Failed to load customer");
        }

    }

    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCustomerId.setText(generateNewCustomerId());
        init();
    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws FailedOperationException, DuplicateException {
        String id = txtCustomerId.getText();
        String name = txtCustomerName.getText();
        String address = txtCustomerAddress.getText();

        if (!name.trim().matches("[A-Za-z ]+")) {
            txtCustomerName.requestFocus();
            txtCustomerName.setFocusColor(Paint.valueOf("red"));
            txtCustomerName.selectAll();
            return;
        }
        if (!address.trim().matches("[A-Za-z0-9 ]+")) {
            txtCustomerAddress.requestFocus();
            txtCustomerAddress.setFocusColor(Paint.valueOf("red"));
            txtCustomerAddress.selectAll();
            return;
        }

        if (btnSave.getText().equals("Save")) {
            try {
                customerService.saveCustomer(new CustomerDTO(id, name, address));
            } catch (FailedOperationException | DuplicateException | SQLException e) {
                throw new FailedOperationException("Failed to execute save operation");
            }
            tblCustomers.getItems().add(new CustomerTM(id, name, address));
            init();

        } else if (btnSave.getText().equals("Update")) {
            try {
                customerService.updateCustomer(new CustomerDTO(id, name, address));
            } catch (NotFondException | SQLException e) {
                throw new DuplicateException("Failed to update the customer");
            }
            tblCustomers.getSelectionModel().getSelectedItem().setCustomerName(name);
            tblCustomers.getSelectionModel().getSelectedItem().setCustomerAddress(address);
            tblCustomers.refresh();
            init();
        }
    }

    public void init() {
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerName.requestFocus();
        txtCustomerId.setText(generateNewCustomerId());
        tblCustomers.getSelectionModel().clearSelection();
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        try {
            customerService.deleteCustomer(tblCustomers.getSelectionModel().getSelectedItem().getCustomerId());
        } catch (FailedOperationException e) {
            throw new FailedOperationException("Failed to delete the customer");
        }
        tblCustomers.getItems().remove(tblCustomers.getSelectionModel().getSelectedItem());
    }

    public String generateNewCustomerId() {

        int lastIdNumerical = 0;

        if (tblCustomers.getItems().isEmpty()) {
            return "C001";
        } else {
            ObservableList<CustomerTM> items = tblCustomers.getItems();
            for (CustomerTM customerTM : items) {
                if (lastIdNumerical < Integer.parseInt(customerTM.getCustomerId().split("C")[1])) {
                    lastIdNumerical = Integer.parseInt(customerTM.getCustomerId().split("C")[1]);
                }
            }
            return String.format("C%03d", lastIdNumerical + 1);

        }
    }
}
