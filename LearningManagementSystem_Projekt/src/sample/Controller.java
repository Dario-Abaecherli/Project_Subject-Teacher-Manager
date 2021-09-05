package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    // Einbindung von UI elementen
    @FXML
    private TextField txt_Prename;
    @FXML
    private TextField txt_Lastname;
    @FXML
    private TextField txt_TeacherShort;
    @FXML
    private TextField txt_SubjectShort;
    @FXML
    private TextField txt_Description;
    @FXML
    private TextField txt_Aims;
    @FXML
    private TextField txt_Resume;
    @FXML
    private TableView tableView_Data;


    // Einbindung Button
    @FXML
    private void Click_btn_Upload(ActionEvent event) throws SQLException {
        Connection conn = SQLConnection.getConnection();
        if(txt_Prename.getText().trim().length() > 2 && txt_Lastname.getText().trim().length() > 2) {
            //TeacherNamen und Nachnamen in strings speichern und gleichzeitig ersten Buchstaben gross schrieben
            String prename = txt_Prename.getText().substring(0,1).toUpperCase() + txt_Prename.getText().substring(1).toLowerCase();
            String lastname = txt_Lastname.getText().substring(0,1).toUpperCase() + txt_Lastname.getText().substring(1).toLowerCase();
            boolean teacherUploaded = false;

            //Teacher Short erstellen und Einfügen
            String teacherShort = lastname.substring(0,1) + lastname.substring(1,2) + prename.substring(0,1);
            txt_TeacherShort.setText(teacherShort);

            //Ist Lehrer Bereits Vorhanden?
            if(getTeacherID(prename, lastname) == -1) {
                //Set new TeacherShort as long as it is not Unique
                int i = 2;
                teacherUploaded = true;

                while(!isUniqueShort(teacherShort) && i < lastname.length()-1){
                    teacherShort = lastname.substring(0,1).toUpperCase() + lastname.substring(i,i+1).toLowerCase() + prename.substring(0,1).toUpperCase();
                    i++;
                }
                txt_TeacherShort.setText(teacherShort);

                if(isUniqueShort(teacherShort)) {
                    //Insert Values into DB
                    conn.createStatement().executeUpdate("INSERT INTO teacher (TeacherPrename, TeacherLastname, TeacherShort) VALUES ('" + prename + "', '" + lastname + "', '" + teacherShort + "')");
                }
                else{
                    messageBox("All TeacherShorts possible for "+prename+" "+lastname+" are already in use");
                }
            }

            // Abfrage, ob Subject Felder Leer sind
            if(!txt_SubjectShort.getText().trim().isEmpty() && !txt_Description.getText().trim().isEmpty()){
                //Variabeln für Subjekt erstellen
                String subjectShort = txt_SubjectShort.getText();
                String description = txt_Description.getText();
                int teacherID = getTeacherID(prename, lastname);

                if(isUniqueSubject(subjectShort)) {
                    //Datenbankeinfügung von Fach angebunden an den Lehrer
                    conn.createStatement().executeUpdate(
                            "INSERT INTO subject (FK_TeacherID, SubjectShort, SubjectDescription) VALUES " +
                                    "(" + teacherID + ", '" + subjectShort + "', '" + description + "')");

                    txt_SubjectShort.setText("");
                    txt_Description.setText("");
                }
                // Es Folgen fehlermendungen bei Eingabeproblemen
                else{
                    messageBox("Subject already exists in Database");
                    txt_SubjectShort.setText("");
                }
            }
            // Wenn kein Lehrer Hochgeladen wurde (Lehrer bereits in DB)
            else if(!teacherUploaded){
                messageBox("Both Subject short and description must be filled in");
            }
        }
        else if(txt_Prename.getText().trim().isEmpty() || txt_Lastname.getText().trim().isEmpty()){
            messageBox("Teacher must be Selected");
        }
        else{
            messageBox("Teacher First and Lastname must be at least 3 Characters");
        }
        conn = null;
    }

    // Combobox hinzufügen Teachers in TableView
    @FXML
    private void Click_btn_ShowTeacher(ActionEvent event) throws SQLException {
        Connection conn = SQLConnection.getConnection();

        ResultSet resultSet = conn.createStatement().executeQuery("SELECT TeacherPrename, TeacherLastname, TeacherShort FROM teacher");
        //Eigene Funkiton zum Füllen des TableView
        fillTableView(resultSet, false);

        conn = null;
    }

    @FXML
    private void Click_btn_ShowSubject(ActionEvent event) throws SQLException {
        Connection conn = SQLConnection.getConnection();

        ResultSet resultSet = conn.createStatement().executeQuery("SELECT TeacherPrename, TeacherLastname, TeacherShort, SubjectShort, SubjectDescription FROM teacher JOIN subject ON teacher.TeacherID = subject.FK_TeacherID");
        fillTableView(resultSet, true);

        conn = null;
    }

    @FXML
    private void Click_btn_SelectItem(ActionEvent event){
        Map selectedItem = (Map)tableView_Data.getSelectionModel().getSelectedItem();

        if(selectedItem != null){
            //Daten in Datenfeld einfügen
            txt_Prename.setText(selectedItem.get("firstName").toString());
            txt_Lastname.setText(selectedItem.get("lastName").toString());
            txt_TeacherShort.setText(selectedItem.get("teacherShort").toString());

            if(selectedItem.containsKey("subjectShort")){
                txt_SubjectShort.setText(selectedItem.get("subjectShort").toString());
            }
            else{
                txt_SubjectShort.setText("");
            }

            if(selectedItem.containsKey("SubjectDescription")){
                txt_Description.setText(selectedItem.get("SubjectDescription").toString());
            }
            else{
                txt_Description.setText("");
            }
        }
    }

    @FXML
    private void Click_txt_Aims(ActionEvent event){

    }
    @FXML
    private void Click_txt_Resume(ActionEvent event){

    }

    // Abfrage der Datenbank, ob lehrer bereits existiert und wenn ja Rückgabe der ID
    private int getTeacherID(String teacherName, String teacherLastname) throws SQLException {
        Connection conn = SQLConnection.getConnection();
        int teacherID = 0;

        ResultSet resultSet = conn.createStatement().executeQuery("SELECT TeacherID FROM teacher WHERE TeacherPrename = '"+teacherName+"' AND TeacherLastname ='"+teacherLastname+"'");
        if(!resultSet.next()) {
            teacherID = -1;
        }
        else{
            teacherID = ((Number) resultSet.getObject(1)).intValue();
        }


        conn = null;
        return teacherID;
    }

    // Prüfen, ob Teachershort unique ist
    private boolean isUniqueShort(String teacherShort) throws SQLException{
        Connection conn = SQLConnection.getConnection();
        boolean unique = false;

        ResultSet resultSet = conn.createStatement().executeQuery("SELECT TeacherID FROM teacher WHERE TeacherShort = '"+teacherShort+"'");
        if(!resultSet.next()) {
            unique = true;
        }

        conn = null;
        return unique;
    }

    // Prüfen, of SubjectShort unique ist
    private boolean isUniqueSubject(String subject) throws SQLException {
        Connection conn = SQLConnection.getConnection();
        boolean unique = false;

        ResultSet resultSet = conn.createStatement().executeQuery("SELECT SubjectID FROM subject WHERE SubjectShort = '"+subject+"'");
        if(!resultSet.next()) {
            unique = true;
        }

        conn = null;
        return unique;
    }

    // Fehlerbox
    private void messageBox(String problemText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Problem");
        alert.setHeaderText(problemText);
        alert.showAndWait();
    }

    // Füllen von Tableview
    private void fillTableView(ResultSet resultSet, boolean subjectInfo) throws SQLException{
        tableView_Data.getItems().clear();
        tableView_Data.getColumns().clear();

        // Tabellenkolonnen erstellen und Nahmen geben
        TableColumn<Map, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new MapValueFactory<>("firstName"));

        TableColumn<Map, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new MapValueFactory<>("lastName"));

        TableColumn<Map, String> teacherShortColumn = new TableColumn<>("Teacher Short");
        teacherShortColumn.setCellValueFactory(new MapValueFactory<>("teacherShort"));


        tableView_Data.getColumns().add(firstNameColumn);
        tableView_Data.getColumns().add(lastNameColumn);
        tableView_Data.getColumns().add(teacherShortColumn);

        //
        if(subjectInfo){
            TableColumn<Map, String> subjectShortColumn = new TableColumn<>("Subject");
            subjectShortColumn.setCellValueFactory(new MapValueFactory<>("subjectShort"));
            TableColumn<Map, String> subjectDescriptionColumn = new TableColumn<>("Subject Description");
            subjectDescriptionColumn.setCellValueFactory(new MapValueFactory<>("SubjectDescription"));
            tableView_Data.getColumns().add(subjectShortColumn);
            tableView_Data.getColumns().add(subjectDescriptionColumn);
        }

        ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

        while (resultSet.next()) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("firstName", resultSet.getString(1));
            item1.put("lastName", resultSet.getString(2));
            item1.put("teacherShort", resultSet.getString(3));

            if(subjectInfo){
                item1.put("subjectShort", resultSet.getString(4));
                item1.put("SubjectDescription", resultSet.getString(5));
            }
            items.add(item1);
        }
        tableView_Data.getItems().addAll(items);
    }
}
