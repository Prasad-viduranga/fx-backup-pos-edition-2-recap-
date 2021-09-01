package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import lk.ijse.dep7.util.PlaceOrderTM;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;

public class PlaceOrderFormController {

    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public TableView<PlaceOrderTM> tblOrderDetails;
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
        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        TableColumn<PlaceOrderTM, Button> deleteCol = (TableColumn<PlaceOrderTM, Button>) tblOrderDetails.getColumns().get(5);


        btnAdd.setDisable(true);
        lblId.setText(generateNewOrderId());
        lblDate.setText(String.valueOf(LocalDate.now()));
        txtCustomerName.setEditable(false);
        txtDescription.setEditable(false);
        txtQtyOnHand.setEditable(false);
        txtUnitPrice.setEditable(false);

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
        cmbCustomerId.requestFocus();

        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedCustomer) -> {
            if (selectedCustomer != null) {
                try {
                    txtCustomerName.setText(customerService.findCustomer((String) selectedCustomer).getCustomerName());
                } catch (NotFondException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the customer name");
                }
                if (cmbItemCode.getSelectionModel().getSelectedItem() != null) {
                    btnAdd.setDisable(false);
                    txtQty.requestFocus();
                } else {
                    cmbItemCode.requestFocus();
                }
            }
        });

        txtQty.setOnAction(event -> btnAdd.fire());

        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedItem) -> {
            if (selectedItem != null) {
                try {
                    txtDescription.setText(itemService.findItem(selectedItem.toString()).getDescription());
                    txtQtyOnHand.setText(itemService.findItem(selectedItem.toString()).getQtyOnHand());
                    txtUnitPrice.setText(itemService.findItem(selectedItem.toString()).getUnitPrice());
                } catch (NotFondException e) {
                    e.printStackTrace();
                }
                if (cmbCustomerId.getSelectionModel().getSelectedItem() != null) {
                    btnAdd.setDisable(false);
                    txtQty.requestFocus();
                } else {
                    cmbCustomerId.requestFocus();
                }
            }
        });

    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
        if ((!txtQty.getText().trim().matches("[0-9 ]+")) || (Integer.parseInt(txtQty.getText().trim()) > Integer.parseInt(txtQtyOnHand.getText().trim()))) {
            txtQty.requestFocus();
            txtQty.setFocusColor(Paint.valueOf("red"));
            return;
        }
        tblOrderDetails.getItems().add(new PlaceOrderTM((String) cmbItemCode.getValue(),txtDescription.getText(),
                txtQty.getText(),new BigDecimal(txtUnitPrice.getText()),new BigDecimal(txtUnitPrice.getText()).multiply(new BigDecimal(txtQty.getText()))));

    }

    public void txtQty_OnAction(ActionEvent actionEvent) {

    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {

    }

    public String generateNewOrderId() {

        int lastIdNumerical = 0;

        if (tblOrderDetails.getItems().isEmpty()) {
            return "OD001";
        } else {
            ObservableList<PlaceOrderTM> items = tblOrderDetails.getItems();
            for (PlaceOrderTM placeOrderTM : items) {
                if (lastIdNumerical < Integer.parseInt(placeOrderTM.getCode().split("OD")[1])) {
                    lastIdNumerical = Integer.parseInt(placeOrderTM.getCode().split("OD")[1]);
                }
            }
            return String.format("OD%03d", lastIdNumerical + 1);

        }
    }
}
