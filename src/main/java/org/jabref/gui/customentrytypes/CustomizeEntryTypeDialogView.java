package org.jabref.gui.customentrytypes;

import java.util.EnumSet;

import javax.inject.Inject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.jabref.gui.customentrytypes.CustomEntryTypeDialogViewModel.FieldType;
import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.util.BaseDialog;
import org.jabref.gui.util.ValueTableCellFactory;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntryType;
import org.jabref.model.entry.field.Field;
import org.jabref.preferences.PreferencesService;

import com.airhacks.afterburner.views.ViewLoader;
import org.fxmisc.easybind.EasyBind;

public class CustomizeEntryTypeDialogView extends BaseDialog<Void> {

    @FXML private TableView<BibEntryType> entryTypes;
    @FXML private TableColumn<BibEntryType, String> entryTypColumn;
    @FXML private TableColumn<BibEntryType, String> entryTypeActionsColumn;
    @FXML private TextField addNewEntryType;
    @FXML private TableView<FieldViewModel> requiredFields;
    @FXML private TableColumn<FieldViewModel, String> fieldNameColumn;
    @FXML private TableColumn<FieldViewModel, FieldType> fieldTypeColumn;
    @FXML private TableColumn<FieldViewModel, String> fieldTypeActionColumn;
    @FXML private ComboBox<Field> addNewField;
    @FXML private ButtonType applyButton;

    @Inject PreferencesService preferencesService;

    private final CustomEntryTypeDialogViewModel viewModel;
    private BibDatabaseMode mode;

    public CustomizeEntryTypeDialogView(BibDatabaseContext bibDatabaseContext) {

        viewModel = new CustomEntryTypeDialogViewModel(mode);
        this.mode = bibDatabaseContext.getMode();

        ViewLoader.view(this)
                  .load()
                  .setAsDialogPane(this);
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    private void setupTable() {

        entryTypColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getType().getDisplayName()));
        entryTypes.itemsProperty().bind(viewModel.entryTypesProperty());
        entryTypes.getSelectionModel().selectFirst();

        
        entryTypeActionsColumn.setSortable(false);
        entryTypeActionsColumn.setReorderable(false);
        entryTypeActionsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getType().getDisplayName()));
        new ValueTableCellFactory<BibEntryType, String>()
             .withGraphic(item -> IconTheme.JabRefIcons.DELETE_ENTRY.getGraphicNode())
             .withTooltip(name -> Localization.lang("Remove entry type") + " " + name)
             .withOnMouseClickedEvent(item -> evt -> viewModel.removeEntryType(entryTypes.getFocusModel().getFocusedItem()))
             .install(entryTypeActionsColumn);

        fieldTypeColumn.setCellFactory(cellData -> new RadioButtonCell<>(EnumSet.allOf(FieldType.class)));
        fieldTypeColumn.setCellValueFactory(item -> item.getValue().fieldTypeProperty());
        fieldNameColumn.setCellValueFactory(item -> item.getValue().fieldNameProperty());

        viewModel.selectedEntryTypeProperty().bind(entryTypes.getSelectionModel().selectedItemProperty());
        viewModel.entryTypeToAddProperty().bind(addNewEntryType.textProperty());

        addNewField.setItems(viewModel.fieldsProperty());
        addNewField.setConverter(viewModel.fieldStringConverter);

        fieldTypeActionColumn.setSortable(false);
        fieldTypeActionColumn.setReorderable(false);
        fieldTypeActionColumn.setCellValueFactory(cellData -> cellData.getValue().fieldNameProperty());

        new ValueTableCellFactory<FieldViewModel, String>()
           .withGraphic(item -> IconTheme.JabRefIcons.DELETE_ENTRY.getGraphicNode())
           .withTooltip(name -> Localization.lang("Remove field from entry type") + " " + name)
           .withOnMouseClickedEvent(item -> evt -> viewModel.removeField(requiredFields.getFocusModel().getFocusedItem()))
           .install(fieldTypeActionColumn);

        viewModel.newFieldToAddProperty().bind(addNewField.valueProperty());
        requiredFields.itemsProperty().bindBidirectional(viewModel.fieldsforTypesProperty());

        EasyBind.subscribe(requiredFields.getSelectionModel().selectedItemProperty(), field -> {
            System.out.println("selected field " + field);
        });

    }

    @FXML
    void addEntryType() {
        viewModel.addNewCustomEntryType();
    }

    @FXML
    void addNewField() {
        viewModel.addNewField();

    }

}