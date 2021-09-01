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
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFondException;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.util.CustomerTM;
import lk.ijse.dep7.util.ItemTM;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class ManageItemsFormController {

    public AnchorPane root;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public TableView<ItemTM> tblItems;
    public JFXTextField txtUnitPrice;
    private final ItemService itemService = new ItemService();


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
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        txtCode.setEditable(false);
        txtCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(true);
        loadAllItems();

        tblItems.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            if (newValue != null) {
                btnSave.setText("Update");
                btnSave.setDisable(false);
                txtCode.setDisable(false);
                txtDescription.setDisable(false);
                txtQtyOnHand.setDisable(false);
                txtUnitPrice.setDisable(false);
                txtCode.setText(newValue.getItemCode());
                txtDescription.setText(newValue.getDescription());
                txtQtyOnHand.setText(newValue.getQtyOnHand());
                txtUnitPrice.setText(newValue.getUnitPrice());
            } else {
                btnSave.setText("Save");
            }
        });
        txtDescription.setOnAction(event -> txtQtyOnHand.requestFocus());
        txtQtyOnHand.setOnAction(event -> txtUnitPrice.requestFocus());
        txtUnitPrice.setOnAction(event -> btnSave.fire());

    }

    private void loadAllItems() {
        List<ItemDTO> allItems = null;
        try {
            allItems = itemService.findAllItems();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to load items"+e.getMessage());
        }
        for (ItemDTO itemDTO : allItems) {
            tblItems.getItems().add(new ItemTM(itemDTO.getItemCode(), itemDTO.getDescription(), itemDTO.getQtyOnHand(), itemDTO.getUnitPrice()));
        }
    }


    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        btnSave.setDisable(false);
        txtCode.setText(generateNewItemId());
        init();
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        itemService.deleteItem(tblItems.getSelectionModel().getSelectedItem().getItemCode());
        tblItems.getItems().remove(tblItems.getSelectionModel().getSelectedItem());

    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws FailedOperationException, DuplicateException, NotFondException {
        String id = txtCode.getText();
        String description = txtDescription.getText();
        String qtyOnHand = txtQtyOnHand.getText();
        String unitPrice = txtUnitPrice.getText();


        if (!description.trim().matches("[A-Za-z0-9 ]+")) {
            txtDescription.requestFocus();
            txtDescription.setFocusColor(Paint.valueOf("red"));
            txtDescription.selectAll();
            return;
        }
        if (!qtyOnHand.trim().matches("[A-Za-z0-9 ]+")) {
            txtQtyOnHand.requestFocus();
            txtQtyOnHand.setFocusColor(Paint.valueOf("red"));
            txtQtyOnHand.selectAll();
            return;
        }
        if (!unitPrice.trim().matches("[0-9. ]+")) {
            txtUnitPrice.requestFocus();
            txtUnitPrice.setFocusColor(Paint.valueOf("red"));
            txtUnitPrice.selectAll();
            return;
        }

        if (btnSave.getText().equals("Save")) {
            try {
                itemService.saveItem(new ItemDTO(id, description, qtyOnHand,unitPrice));
            } catch (DuplicateException e) {
                throw new FailedOperationException("Failed to execute save operation");
            }
            tblItems.getItems().add(new ItemTM(id, description, qtyOnHand,unitPrice));
            init();

        } else if (btnSave.getText().equals("Update")) {

            try {
                itemService.updateItem(new ItemDTO(id, description, qtyOnHand,unitPrice));
            } catch (NotFondException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new NotFondException("Failed to update the customer");
            }
            tblItems.getSelectionModel().getSelectedItem().setDescription(description);
            tblItems.getSelectionModel().getSelectedItem().setQtyOnHand(qtyOnHand);
            tblItems.getSelectionModel().getSelectedItem().setUnitPrice(unitPrice);
            tblItems.refresh();
            init();
        }
    }

    public void init() {
        txtCode.setDisable(false);
        txtDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        txtDescription.requestFocus();
        txtCode.setText(generateNewItemId());
        tblItems.getSelectionModel().clearSelection();
    }

    public String generateNewItemId() {

        int lastIdNumerical = 0;

        if (tblItems.getItems().isEmpty()) {
            return "I001";
        } else {
            ObservableList<ItemTM> items = tblItems.getItems();
            for (ItemTM itemTM : items) {
                if (lastIdNumerical < Integer.parseInt(itemTM.getItemCode().split("I")[1])) {
                    lastIdNumerical = Integer.parseInt(itemTM.getItemCode().split("I")[1]);
                }
            }
            return String.format("I%03d", lastIdNumerical + 1);
        }
    }
}
