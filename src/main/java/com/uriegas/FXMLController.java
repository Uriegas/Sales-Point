package com.uriegas;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.*;
import com.uriegas.Model.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.*;
import javafx.util.converter.DoubleStringConverter;
import javafx.beans.property.*;
/**
 * Controller for the main window
 */
public class FXMLController implements Initializable {
    DataModel model;

    @FXML private TextField searchbar;
    @FXML private Button search_btn;
    @FXML private Button clear_btn;
    @FXML private Button settings_btn;
    @FXML private TableView<Searchable> searchableproducts;
    @FXML private TableColumn<Searchable, String> productname;
    @FXML private TableColumn<Searchable, String> productdescription;
    @FXML private TableColumn<Searchable, Integer> productid;
    @FXML private TableColumn<Searchable, Void> carrito;
    @FXML private TableView<Product> sales;
    @FXML private TableColumn<Product, String> salesproductname;
    @FXML private TableColumn<Product, Integer> salesproductquantity;
    @FXML private TableColumn<Product, Double> salesproductprice;
    @FXML private Button commitsale;
    @FXML private Label totalprice;
    @FXML private Button addbtn;
    @FXML private Button updatebtn;
    @FXML private Button deletebtn;
    DoubleProperty totalpriceProperty = new SimpleDoubleProperty(0.00);
    /**
     * Get the model
     */
    public void initModel(DataModel model) {    
        if(this.model != null)
            throw new NullPointerException("Model is already initialized, can only initialize it once");
        else
            this.model = model;
        // ==> Data binding
        FilteredList<Searchable> filteredList = new FilteredList<>(this.model.getSearchables());
        searchableproducts.setItems(filteredList);
        searchbar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(createPredicate(newValue));
        });
        // totalprice.textProperty().bind(this.model.getCart().forEach(p -> p.getPrice() * p.getStock() ).sum());
        this.sales.setItems(this.model.cartProperty());
        // this.searchableproducts.setItems(this.model.getSearchables());
        // totalprice.textProperty().bindBidirectional(model.sProperty());
        this.totalprice.textProperty().bind(this.model.totalpriceProperty().asString());
        // <== Data binding
        
    }
    /**
     * Constructor
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.salesproductname.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.salesproductquantity.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        this.salesproductprice.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        // StringConverter<? extends Number> converter = new DoubleStringConverter();

        this.productname.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.productdescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        this.productid.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        // settings_btn.setOnAction(event -> {
        //     //Create dialog to choose between add a product or a offert
        //     ChoiceDialog<String> dialog = new ChoiceDialog<>("Add/modify product", "Add/modify offer");
        //     dialog.setHeaderText("Settings");
        //     dialog.setContentText("Choose what you want to do");
        //     // dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        //     dialog.showAndWait().ifPresent(choice -> {
        //         if(choice.equals("Add/modify product"))//Create dialog for the selected product
        //             // addProductDialog((Product)searchableproducts.getSelectionModel().getSelectedItem());
        //         else if(choice.equals("Add/modify offer"))//Create dialog to choose between add a product or a offert
        //             addOfferDialog();
        //     });
        // });
        Callback<TableColumn<Searchable, Void>, TableCell<Searchable, Void>> cellFactory = new Callback<TableColumn<Searchable, Void>, TableCell<Searchable, Void>>() {
            @Override
            public TableCell<Searchable, Void> call(TableColumn<Searchable, Void> param) {
                return new TableCell<Searchable, Void>() {
                    final Button btn = new Button("Agregar");
                    {
                        btn.setOnAction(event -> {
                            // Searchable s = searchableproducts.getSelectionModel().getSelectedItem();
                            Searchable s = (Searchable) getTableView().getItems().get(getIndex());
                            System.out.println("INFO " + s.toString());
                            model.addToCart(s);
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty)
                            setGraphic(null);
                        else 
                            setGraphic(btn);
                    }
                };
            }
        };
        this.carrito.setCellFactory(cellFactory);

        addbtn.setOnAction(event -> {
            addProductDialog(new Product());
            
        });
        updatebtn.setOnAction(event -> {
            updateProductDialog((Product)searchableproducts.getSelectionModel().getSelectedItem());
        });
        commitsale.setOnAction(event -> {
            try{
                this.model.makeSale();
                //Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sale");
                alert.setHeaderText("Sale successful");
                alert.setContentText("The sale was successful");
                alert.showAndWait();
            }catch(Exception e){//Show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(e.getClass().getName());
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }
    /**
     * Dialog to delete a product
     * @param p selected product
     */
    private void deleteProductDialog(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete product");
        alert.setHeaderText("Delete product");
        alert.setContentText("Are you sure you want to delete this product?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try{
                model.deleteProduct(product);
            }catch(Exception e){
                Alert alert2 = new Alert(Alert.AlertType.ERROR);
                alert2.setTitle("Error");
                alert2.setHeaderText("An error ocurred");
                alert2.setContentText("Could not delete this product");
                alert2.showAndWait();
            }
        }
    }
    /**
     * Dialog to add a product
     * @param p an empty product
     */
    private void addProductDialog(Product p) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductDialog.fxml"));
            Parent root = loader.load();
            ProductDialogController controller = loader.getController();
            controller.setProduct(p);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add/modify product");
            dialog.setDialogPane((DialogPane)root);
            dialog.showAndWait().ifPresent(button -> {
                if(button.equals(ButtonType.OK)){//Save product to database
                    System.out.println("INFO " + p.toString());
                    this.model.addNewProduct(p);
                }
            });
        }catch(Exception e){//Show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Couldn't perform this action.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Dialog to update a product
     * @param p the selected product
     */
    private void updateProductDialog(Product p){
        try{
            final Product copy = p.clone();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductDialog.fxml"));
            Parent root = loader.load();
            ProductDialogController controller = loader.getController();
            controller.setProduct(p);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Update product");
            dialog.setDialogPane((DialogPane)root);
            dialog.showAndWait().ifPresent(button -> {
                if(button.equals(ButtonType.CANCEL)){
                    p.setName(copy.getName());
                    p.setDescription(copy.getDescription());
                    p.setId(copy.getId());
                    p.setStock(copy.getStock());
                    p.setPrice(copy.getPrice());
                    System.out.println("Cancel");
                }
            });
        }catch(Exception e){//Show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Make your that you have selected a product");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    private void addOfferDialog(){
        System.out.println("Add/modify offer");
    }
    private boolean searchFind(Searchable s, String search){
        return String.valueOf(s.getId()).contains(search) || s.getName().toString().toLowerCase().contains(search.toLowerCase()) || s.getDescription().toString().toLowerCase().contains(search.toLowerCase());
    }
    private Predicate<Searchable> createPredicate(String searchText){
        return searchable -> {
            if (searchText == null || searchText.isEmpty()) return true;
            return searchFind(searchable, searchText);
        };
    }
}
