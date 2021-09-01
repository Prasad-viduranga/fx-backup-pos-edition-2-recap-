CREATE TABLE customer(
    id VARCHAR(10) PRIMARY KEY ,
    name VARCHAR (50) NOT NULL ,
    address VARCHAR (100) NOT NULL
);

CREATE TABLE item(
    itemCode VARCHAR(10) PRIMARY KEY ,
    itemDescription VARCHAR(50) NOT NULL ,
    itemQtyOnHand INT NOT NULL ,
    itemUnitPrice DECIMAL NOT NULL
);
INSERT INTO customer (id,name,address) VALUES ('C011','Pakkmkl','mmklnn');