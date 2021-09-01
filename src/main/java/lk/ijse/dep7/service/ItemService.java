package lk.ijse.dep7.service;

import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFondException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    Connection connection = SingleConnectionDataSource.getInstance().getConnection();


    public List<ItemDTO> findAllItems() throws FailedOperationException {
        List<ItemDTO> itemDTOList = new ArrayList<>();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM item");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                itemDTOList.add(new ItemDTO(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4)));
            }
            return itemDTOList;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to load items");
        }
    }

    public ItemDTO findItem(String itemCode) throws NotFondException {
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM item WHERE itemCode=?");
            stm.setString(1, itemCode);
            ResultSet rst = stm.executeQuery();
            rst.next();
            return new ItemDTO(rst.getString(1), rst.getString(2), rst.getString(3),rst.getString(4));
        } catch (SQLException e) {
            throw new NotFondException("That item code already exist");
        }
    }

    public void saveItem(ItemDTO itemDTO) throws DuplicateException {


        try {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO item (itemCode, itemDescription, itemQtyOnHand, itemUnitPrice) VALUES (?,?,?,?)");
            stm.setString(1, itemDTO.getItemCode());
            stm.setString(2, itemDTO.getDescription());
            stm.setString(3, itemDTO.getQtyOnHand());
            stm.setString(4, itemDTO.getUnitPrice());
            stm.executeUpdate();


        } catch (SQLException e) {
            throw new DuplicateException("That item code already exist");
        }

    }

    public void deleteItem(String id) {
        try {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM item WHERE itemCode=?");
            stm.setString(1, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateItem(ItemDTO itemDTO) throws NotFondException, SQLException {
        if (!exists(itemDTO.getItemCode())) {
            throw new NotFondException("ID not exists");
        }
        try {
            PreparedStatement stm = connection.prepareStatement("UPDATE item SET itemDescription=?,itemQtyOnHand=?,itemUnitPrice=? WHERE itemCode=?");
            stm.setString(1, itemDTO.getDescription());
            stm.setString(2, itemDTO.getQtyOnHand());
            stm.setString(3, itemDTO.getUnitPrice());
            stm.setString(4, itemDTO.getItemCode());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new NotFondException("Failed to update the customer");
        }

    }

    public boolean exists(String id) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT itemCode FROM item WHERE itemCode=?");
        stm.setString(1, id);
        return stm.executeQuery().next();

    }

}
