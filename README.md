# ORM FRAMEWORK

### USING REFLECTION API IN JAVA :fire:

This framework is a gradle project.
And it's build for simplify JDBC part of an application and framework 
itself write queries to create databases
by looking entities.

Data-Base = MySQL


## How to use this framework:

####pass properties file and entity objects to init method in ORMUtil class.

eg:-
ORMUtil.init(properties, Customer.class, Item.class);

####please put @Entity annotation before entity class and @Id annotation before properties in entity

eg:-
````
@Entity
public class Item {

    @Id
    @Column
    private String code;
    @Column
    private String description;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @Column(name = "qty_on_hand")
    private int qtyOnHand;

    ...
}
