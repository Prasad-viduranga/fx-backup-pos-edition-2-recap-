package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFondException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.service.ItemService;

import java.io.IOException;
import java.net.URL;

public class PlaceOrderFormController {

    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox cmbCustomerId;
    public JFXComboBox cmbItemCode;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    public CustomerService customerService = new CustomerService();
    public ItemService itemService = new ItemService();
    public JFXButton btnAdd;

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

    public void initialize() {
        try {
            for (CustomerDTO customerDTO : customerService.findAllCustomer()) {
                cmbCustomerId.getItems().add(customerDTO.getCustomerId());
            }
            for (ItemDTO itemDTO : itemService.findAllItems()) {
                cmbItemCode.getItems().add(itemDTO.getItemCode());
            }
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load the customer IDs");
        }
        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedCustomer) -> {
            if (selectedCustomer != null) {
                try {
                    txtCustomerName.setText(customerService.findCustomer((String) selectedCustomer).getCustomerName());
                } catch (NotFondException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the customer name");
                }
                if (cmbItemCode.getSelectionModel().getSelectedItem() != null) btnAdd.setDisable(false);
            }
        });

        btnAdd.setDisable(true);
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedItem) -> {
            if (selectedItem != null) {
                try {
                    txtDescription.setText(itemService.findItem(selectedItem.toString()).getDescription());
                    txtQtyOnHand.setText(itemService.findItem(selectedItem.toString()).getQtyOnHand());
                    txtUnitPrice.setText(itemService.findItem(selectedItem.toString()).getUnitPrice());
                } catch (NotFondException e) {
                    e.printStackTrace();
                }
                if (cmbCustomerId.getSelectionModel().getSelectedItem() != null) btnAdd.setDisable(false);
            }
        });


    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
        if ((!txtQty.getText().trim().matches("[0-9 ]+")) || (Integer.parseInt(txtQty.getText().trim()) > Integer.parseInt(txtQtyOnHand.getText().trim()))) {
            txtQty.requestFocus();
            txtQty.setFocusColor(Paint.valueOf("red"));
        }

    }

    public void txtQty_OnAction(ActionEvent actionEvent) {
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
    }
}
