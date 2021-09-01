package lk.ijse.dep7.service;

import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFondException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    private final Connection connection = SingleConnectionDataSource.getInstance().getConnection();

    public boolean exists(String id) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT id FROM customer WHERE  id=?");
        stm.setString(1, id);
        return stm.executeQuery().next();

    }

    public void saveCustomer(CustomerDTO customerDTO) throws FailedOperationException, DuplicateException, SQLException {
        if (exists(customerDTO.getCustomerId())) {
            throw new DuplicateException("ID is already exists");
        }
        try {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO customer (id,name,address) Values (?,?,?)");
            stm.setString(1, customerDTO.getCustomerId());
            stm.setString(2, customerDTO.getCustomerName());
            stm.setString(3, customerDTO.getCustomerAddress());
            int affectedRow = stm.executeUpdate();

        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the customer");

        }

    }

    public void updateCustomer(CustomerDTO customerDTO) throws NotFondException, SQLException {
        if (!exists(customerDTO.getCustomerId())) {
            throw new NotFondException("ID not exists");
        }
        try {
            PreparedStatement stm = connection.prepareStatement("UPDATE customer SET name=?,address=? WHERE id=?");
            stm.setString(1, customerDTO.getCustomerName());
            stm.setString(2, customerDTO.getCustomerAddress());
            stm.setString(3, customerDTO.getCustomerId());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new NotFondException("Failed to update the customer");
        }


    }

    public CustomerDTO findCustomer(String customerId) throws NotFondException {
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM customer WHERE id=?");
            stm.setString(1, customerId);
            ResultSet rst = stm.executeQuery();
            rst.next();
            return new CustomerDTO(rst.getString("id"), rst.getString("name"), rst.getString("address"));

        } catch (SQLException e) {
            throw new NotFondException("ID does not exists");
        }

    }

    public List<CustomerDTO> findAllCustomer() throws FailedOperationException {
        List<CustomerDTO> customerDTOList = new ArrayList<>();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM customer");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                customerDTOList.add(new CustomerDTO(rst.getString(1), rst.getString(2), rst.getString(3)));
            }
            return customerDTOList;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to load customer");
        }
    }

    public void deleteCustomer(String customerId) throws FailedOperationException {
        try {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM customer WHERE id=?");
            stm.setString(1, customerId);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete the customer");
        }

    }
}
