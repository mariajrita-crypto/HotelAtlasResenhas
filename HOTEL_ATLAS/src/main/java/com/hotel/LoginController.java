package com.hotel;

import com.hotel.service.CadastroValidator;
import com.hotel.service.ClienteService;
import com.hotel.service.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import java.util.Locale;

public class LoginController {
    @FXML private ComboBox<String> cbPerfil;
    @FXML private TextField txtIdentificador;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;
    @FXML private TextField txtCpfCadastro;
    @FXML private TextField txtTelefoneCadastro;
    @FXML private PasswordField txtSenhaCadastro;
    @FXML private PasswordField txtConfirmarSenha;
    @FXML private Label lblCadastroStatus;

    @FXML public void initialize() {
        cbPerfil.getItems().setAll("Cliente", "Funcionário");
        cbPerfil.setValue("Cliente");
        atualizarIdentificador();
        cbPerfil.setOnAction(e -> atualizarIdentificador());
        lblErro.setText("");
        lblCadastroStatus.setText("Seus dados ficam salvos neste computador.");
        configurarCamposCadastro();
    }

    /** Limita a digitação: CPF e telefone só aceitam números (máx. 11) e o erro some ao corrigir o campo. */
    private void configurarCamposCadastro() {
        somenteDigitos(txtCpfCadastro, CadastroValidator.CPF_DIGITOS);
        somenteDigitos(txtTelefoneCadastro, CadastroValidator.TELEFONE_MAX_DIGITOS);
        for (TextField campo : new TextField[]{txtCpfCadastro, txtTelefoneCadastro, txtSenhaCadastro, txtConfirmarSenha}) {
            campo.textProperty().addListener((obs, antigo, novo) -> campo.getStyleClass().remove(CLASSE_INVALIDO));
        }
    }

    private static final String CLASSE_INVALIDO = "campo-invalido";

    /** Aceita apenas dígitos; colar "123.456.789-01" vira "12345678901" e o excesso além do limite é descartado. */
    private static void somenteDigitos(TextField campo, int maximo) {
        campo.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;
            String digitos = change.getText().replaceAll("\\D", "");
            int substituido = change.getRangeEnd() - change.getRangeStart();
            int vagas = maximo - (change.getControlText().length() - substituido);
            if (digitos.length() > vagas) digitos = digitos.substring(0, Math.max(vagas, 0));
            if (digitos.isEmpty() && !change.getText().isEmpty()) return null; // só tinha letras/símbolos ou não há vaga
            change.setText(digitos);
            return change;
        }));
    }

    private void atualizarIdentificador() {
        boolean funcionario = "Funcionário".equals(cbPerfil.getValue());
        txtIdentificador.setPromptText(funcionario ? "E-mail do funcionário" : "Seu CPF (somente números)");
    }

    @FXML private void entrar() {
        String identificador = txtIdentificador.getText() == null ? "" : txtIdentificador.getText().trim();
        String senha = txtSenha.getText() == null ? "" : txtSenha.getText();
        boolean employee = "Funcionário".equals(cbPerfil.getValue());
        if (identificador.isBlank() || senha.isBlank()) {
            lblErro.setText("Preencha seu CPF/e-mail e sua senha.");
            return;
        }
        try {
            boolean valid;
            if (employee) {
                String email = identificador.toLowerCase(Locale.ROOT);
                valid = email.equals("funcionario@atlas.com") && senha.equals("123456");
                identificador = email;
            } else {
                valid = ClienteService.autenticar(identificador, senha);
                identificador = ClienteService.somenteNumeros(identificador);
            }
            if (!valid) {
                lblErro.setText("Dados incorretos. Confira o acesso e tente novamente.");
                txtSenha.clear();
                txtSenha.requestFocus();
                return;
            }
            Session.login(employee ? "Funcionário Atlas" : "Cliente", identificador, employee ? "FUNCIONARIO" : "CLIENTE");
            abrirTela(employee ? "/com/example/hotel_atlas/main-view.fxml" : "/com/example/hotel_atlas/cliente-view.fxml");
        } catch (Exception e) {
            lblErro.setText("Não foi possível ler os cadastros locais. Verifique as permissões do computador.");
            e.printStackTrace();
        }
    }

    @FXML private void cadastrar() {
        String cpf = txtCpfCadastro.getText();
        String telefone = txtTelefoneCadastro.getText();
        String senha = txtSenhaCadastro.getText();
        String confirmar = txtConfirmarSenha.getText();

        // Marca em vermelho todos os campos com problema e avisa sobre o primeiro (na ordem da tela).
        marcarInvalido(txtCpfCadastro, CadastroValidator.validarCpf(cpf) != null);
        marcarInvalido(txtTelefoneCadastro, CadastroValidator.validarTelefone(telefone) != null);
        CadastroValidator.Erro erroSenha = CadastroValidator.validarSenha(senha, confirmar);
        marcarInvalido(txtSenhaCadastro, erroSenha != null && erroSenha.campo() == CadastroValidator.Campo.SENHA);
        marcarInvalido(txtConfirmarSenha, erroSenha != null && erroSenha.campo() == CadastroValidator.Campo.CONFIRMAR);

        CadastroValidator.Erro erro = CadastroValidator.validar(cpf, telefone, senha, confirmar);
        if (erro != null) {
            estiloStatus(true);
            lblCadastroStatus.setText(erro.mensagem());
            campoDe(erro.campo()).requestFocus();
            return;
        }
        try {
            ClienteService.cadastrar(cpf, telefone, senha);
            estiloStatus(false);
            lblCadastroStatus.setText("Conta criada! Agora entre usando seu CPF e senha.");
            txtIdentificador.setText(ClienteService.somenteNumeros(cpf));
            txtSenha.setText(senha);
            txtCpfCadastro.clear();
            txtTelefoneCadastro.clear();
            txtSenhaCadastro.clear();
            txtConfirmarSenha.clear();
        } catch (IllegalArgumentException e) {
            estiloStatus(true);
            lblCadastroStatus.setText(e.getMessage());
        } catch (Exception e) {
            estiloStatus(true);
            lblCadastroStatus.setText("Não foi possível salvar o cadastro neste computador.");
            e.printStackTrace();
        }
    }

    private void estiloStatus(boolean erro) {
        lblCadastroStatus.getStyleClass().removeAll("error-text", "status-text");
        lblCadastroStatus.getStyleClass().add(erro ? "error-text" : "status-text");
    }

    private TextField campoDe(CadastroValidator.Campo campo) {
        return switch (campo) {
            case CPF -> txtCpfCadastro;
            case TELEFONE -> txtTelefoneCadastro;
            case SENHA -> txtSenhaCadastro;
            case CONFIRMAR -> txtConfirmarSenha;
        };
    }

    private void marcarInvalido(TextField campo, boolean invalido) {
        campo.getStyleClass().remove(CLASSE_INVALIDO);
        if (invalido) campo.getStyleClass().add(CLASSE_INVALIDO);
    }

    private void abrirTela(String caminho) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminho));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) txtIdentificador.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hotel Atlas - " + (Session.isEmployee() ? "Área do Funcionário" : "Área do Cliente"));
        } catch (Exception e) {
            lblErro.setText("Não foi possível abrir sua área. Verifique os arquivos FXML.");
            e.printStackTrace();
        }
    }
}
