package com.hotel;

import com.hotel.service.HotelData;
import com.hotel.service.Session;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class HotelController {
    @FXML private StackPane conteudoCentral;
    @FXML private VBox viewDashboard;
    @FXML private Label lblQuartosTotais, lblQuartosDisponiveis, lblQuartosOcupados, lblQuartosReservados, lblQuartosManutencao;
    @FXML private GridPane gridCalendario;
    @FXML private DatePicker dpDataInicioCalendario;
    @FXML private Label lblUsuario;
    @FXML private TableView<HotelData.Room> tabelaQuartos;
    @FXML private TableColumn<HotelData.Room, Integer> colNumero, colCapacidade;
    @FXML private TableColumn<HotelData.Room, String> colCategoria, colQualidade;
    @FXML private TableColumn<HotelData.Room, java.math.BigDecimal> colDiaria;
    @FXML private TableView<HotelData.Booking> tabelaReservas;
    @FXML private TableView<HotelData.Booking> tabelaHospedes;
    @FXML private TableView<HotelData.PaymentRecord> tabelaFinanceira;
    @FXML private TableColumn<HotelData.Booking, Long> colReservaId;
    @FXML private TableColumn<HotelData.Booking, String> colHospede, colStatus;
    @FXML private TableColumn<HotelData.Booking, LocalDate> colEntrada, colSaida;
    @FXML private TableColumn<HotelData.Booking, HotelData.Room> colQuarto;
    @FXML private TableColumn<HotelData.Booking, java.math.BigDecimal> colValor;

    @FXML
    public void initialize() {

        if (dpDataInicioCalendario != null) {
            dpDataInicioCalendario.setValue(LocalDate.now());
        }

        if (lblUsuario != null) {
            lblUsuario.setText(
                    Session.getName() == null ? "Funcionário" : Session.getName()
            );
        }

        configurarQuartos();
        configurarTabelaReservas();
        configurarTabelasExtras();

        atualizarDashboard();
        carregarCalendarioInteligente(LocalDate.now());
        instalarAcoesAdministrativas();
    }

    private void configurarQuartos() {
    }

    private void configurarTabelasExtras() {
        if (tabelaHospedes != null) {
            tabelaHospedes.getColumns().clear();
            TableColumn<HotelData.Booking,String> nome=new TableColumn<>("Hóspede"); nome.setCellValueFactory(new PropertyValueFactory<>("guestName"));
            TableColumn<HotelData.Booking,String> cpf=new TableColumn<>("CPF"); cpf.setCellValueFactory(new PropertyValueFactory<>("guestCpf"));
            TableColumn<HotelData.Booking,String> quarto=new TableColumn<>("Quarto"); quarto.setCellValueFactory(new PropertyValueFactory<>("room"));
            TableColumn<HotelData.Booking,String> status=new TableColumn<>("Reserva"); status.setCellValueFactory(new PropertyValueFactory<>("status"));
            tabelaHospedes.getColumns().setAll(nome,cpf,quarto,status);
            tabelaHospedes.setItems(FXCollections.observableArrayList(data().getBookings()));
        }
        if (tabelaFinanceira != null) {
            tabelaFinanceira.getColumns().clear();
            TableColumn<HotelData.PaymentRecord,Long> id=new TableColumn<>("ID"); id.setCellValueFactory(new PropertyValueFactory<>("id"));
            TableColumn<HotelData.PaymentRecord,Long> reserva=new TableColumn<>("Reserva"); reserva.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            TableColumn<HotelData.PaymentRecord,String> valor=new TableColumn<>("Valor"); valor.setCellValueFactory(new PropertyValueFactory<>("amount"));
            TableColumn<HotelData.PaymentRecord,String> forma=new TableColumn<>("Forma"); forma.setCellValueFactory(new PropertyValueFactory<>("method"));
            TableColumn<HotelData.PaymentRecord,String> data=new TableColumn<>("Data"); data.setCellValueFactory(new PropertyValueFactory<>("date"));
            tabelaFinanceira.getColumns().setAll(id,reserva,valor,forma,data);
            tabelaFinanceira.setItems(FXCollections.observableArrayList(data().getPayments()));
        }
    }

    private void configurarTabelaQuartos() {
        if (tabelaQuartos == null) return;
        colNumero.setCellValueFactory(new PropertyValueFactory<>("number"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colDiaria.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));
        colQualidade.setCellValueFactory(new PropertyValueFactory<>("quality"));
        tabelaQuartos.setItems(FXCollections.observableArrayList(HotelData.getInstance().getRooms()));
    }

    private void configurarTabelaReservas() {
        if (tabelaReservas == null) return;
        colReservaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHospede.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colQuarto.setCellValueFactory(new PropertyValueFactory<>("room"));
        colEntrada.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colSaida.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("total"));
        tabelaReservas.setItems(FXCollections.observableArrayList(HotelData.getInstance().getBookings()));
    }

    private void atualizarDashboard() {
        if (lblQuartosTotais == null) return;
        int total = HotelData.getInstance().getRooms().size();
        int disponiveis = 0;
        for (HotelData.Room room : HotelData.getInstance().getRooms())
            if (HotelData.getInstance().isAvailable(room, LocalDate.now(), LocalDate.now().plusDays(1))) disponiveis++;
        lblQuartosTotais.setText(String.valueOf(total));
        lblQuartosDisponiveis.setText(String.valueOf(disponiveis));
        lblQuartosOcupados.setText(String.valueOf(total - disponiveis));
        lblQuartosReservados.setText(String.valueOf(HotelData.getInstance().getBookings().size()));
        lblQuartosManutencao.setText("0");
    }

    public void carregarCalendarioInteligente(LocalDate dataInicio) {
        if (gridCalendario == null) return;
        gridCalendario.getChildren().clear();
        String[] dias = {"Quarto", "", "", "", "", "", "", ""};
        for (int col = 0; col < 8; col++) {
            LocalDate diaData = col == 0 ? null : dataInicio.plusDays(col - 1);
            Label header = new Label(col == 0 ? dias[0] : diaData.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("pt", "BR")) + "\n" + diaData.getDayOfMonth() + "/" + diaData.getMonthValue());
            header.getStyleClass().add("calendar-header");
            gridCalendario.add(header, col, 0);
        }
        for (int i = 0; i < HotelData.getInstance().getRooms().size(); i++) {
            HotelData.Room room = HotelData.getInstance().getRooms().get(i);
            Label roomLabel = new Label("Quarto " + room.getNumber()); roomLabel.getStyleClass().add("calendar-room");
            gridCalendario.add(roomLabel, 0, i + 1);
            for (int day = 0; day < 7; day++) {
                LocalDate date = dataInicio.plusDays(day);
                boolean available = HotelData.getInstance().isAvailable(room, date, date.plusDays(1));
                Label cell = new Label(available ? "Livre" : "Reservado");
                cell.getStyleClass().add(available ? "status-disponivel" : "status-reservado");
                cell.setMaxWidth(Double.MAX_VALUE); cell.setMaxHeight(Double.MAX_VALUE);
                gridCalendario.add(cell, day + 1, i + 1);
            }
        }
    }

    @FXML public void abrirDashboard(ActionEvent event) {
        conteudoCentral.getChildren().setAll(viewDashboard);
        atualizarDashboard();
        carregarCalendarioInteligente(dpDataInicioCalendario.getValue() == null ? LocalDate.now() : dpDataInicioCalendario.getValue());
    }
    @FXML public void abrirReservas(ActionEvent event) { carregarTela("/com/example/hotel_atlas/reservas-view.fxml"); }
    @FXML public void abrirQuartos(ActionEvent event) { carregarTela("/com/example/hotel_atlas/quartos-view.fxml"); }
    @FXML public void abrirHospedes(ActionEvent event) { carregarTela("/com/example/hotel_atlas/hospedes-view.fxml"); }
    @FXML public void abrirFinanceiro(ActionEvent event) { carregarTela("/com/example/hotel_atlas/financeiro-view.fxml"); }

    private void carregarTela(String caminho) {
        try { Node tela = new FXMLLoader(getClass().getResource(caminho)).load(); conteudoCentral.getChildren().setAll(tela); }
        catch (IOException e) { new Alert(Alert.AlertType.ERROR, "Erro ao abrir tela: " + e.getMessage()).showAndWait(); e.printStackTrace(); }
    }
    @FXML public void abrirModalNovaReserva(ActionEvent event) { abrirReservas(event); }
    @FXML public void filtrarPorData(ActionEvent event) {
        if (dpDataInicioCalendario.getValue() != null) carregarCalendarioInteligente(dpDataInicioCalendario.getValue());
    }
    @FXML private void sair() {
        try {
            Session.clear();
            Node login = new FXMLLoader(getClass().getResource("/com/example/hotel_atlas/login-view.fxml")).load();
            javafx.stage.Stage stage = (javafx.stage.Stage) conteudoCentral.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) login, 1280, 768));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void instalarAcoesAdministrativas() {
        if (tabelaReservas != null) {
            Button nova = new Button("+ Nova reserva");
            Button status = new Button("Alterar status");
            Button editar = new Button("Editar reserva");
            Button pagamento = new Button("Registrar pagamento");
            estilizarAcoes(nova,status,editar,pagamento);
            nova.setOnAction(e -> novaReservaFuncionario());
            status.setOnAction(e -> alterarStatusReserva());
            editar.setOnAction(e -> editarReservaFuncionario());
            pagamento.setOnAction(e -> registrarPagamento());
            inserirBarra(tabelaReservas, nova,status,editar,pagamento);
            tabelaReservas.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{});
        }
        if (tabelaQuartos != null) {
            Button novo=new Button("+ Novo quarto"), editar=new Button("Editar quarto"), excluir=new Button("Excluir quarto"), manut=new Button("Status / manutenção");
            estilizarAcoes(novo,editar,excluir,manut);
            novo.setOnAction(e->novoQuarto()); editar.setOnAction(e->editarQuarto()); excluir.setOnAction(e->excluirQuarto()); manut.setOnAction(e->statusQuarto());
            inserirBarra(tabelaQuartos,novo,editar,excluir,manut);
        }
        if (tabelaHospedes != null) {
            Button novoHospede=new Button("+ Cadastrar hóspede / reserva");
            Button editarHospede=new Button("Editar hóspede selecionado");
            estilizarAcoes(novoHospede,editarHospede);
            novoHospede.setOnAction(e->novaReservaFuncionario());
            editarHospede.setOnAction(e->editarHospedeSelecionado());
            inserirBarra(tabelaHospedes,novoHospede,editarHospede);
        }
        if (tabelaFinanceira != null) {
            Label info=new Label("Pagamentos registrados nas reservas aparecem no arquivo local pagamentos.json.");
            info.setStyle("-fx-text-fill:#55688d;");
            ((VBox)tabelaFinanceira.getParent()).getChildren().add(1,info);
            tabelaFinanceira.getItems().clear();
        }
    }
    private void estilizarAcoes(Button... bs){for(Button b:bs){b.setStyle("-fx-background-color:#0a3680;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:0;-fx-padding:10 18;");}}
    private void inserirBarra(TableView<?> tabela, Button... botoes){
        if(tabela.getParent() instanceof VBox box){HBox barra=new HBox(9,botoes);barra.setAlignment(javafx.geometry.Pos.CENTER_LEFT);box.getChildren().add(Math.max(0,box.getChildren().indexOf(tabela)),barra);}
    }
    private HotelData data(){return HotelData.getInstance();}
    private HotelData.Booking reservaSelecionada(){HotelData.Booking b=tabelaReservas.getSelectionModel().getSelectedItem();if(b==null)alerta("Selecione uma reserva na tabela.");return b;}
    private void alerta(String msg){new Alert(Alert.AlertType.INFORMATION,msg).showAndWait();}
    private String texto(String titulo,String prompt,String valor){TextInputDialog d=new TextInputDialog(valor==null?"":valor);d.setTitle("Hotel Atlas");d.setHeaderText(titulo);d.setContentText(prompt);return d.showAndWait().orElse(null);}
    private void novaReservaFuncionario(){
        try{
            String cpf=texto("Nova reserva","CPF do hóspede (11 dígitos):","");if(cpf==null)return;
            String nome=texto("Nova reserva","Nome completo:","");if(nome==null)return;
            String q=texto("Nova reserva","Número do quarto:","101");if(q==null)return;
            String ent=texto("Nova reserva","Check-in (AAAA-MM-DD):",LocalDate.now().toString());if(ent==null)return;
            String sai=texto("Nova reserva","Check-out (AAAA-MM-DD):",LocalDate.now().plusDays(1).toString());if(sai==null)return;
            String hosp=texto("Nova reserva","Quantidade de hóspedes:","1");if(hosp==null)return;
            String pg=texto("Nova reserva","Forma de pagamento (PIX, DINHEIRO, CREDITO, DEBITO):","PIX");if(pg==null)return;
            int numero=Integer.parseInt(q),n=Integer.parseInt(hosp);
            HotelData.Room room=data().getRooms().stream().filter(r->r.getNumber()==numero).findFirst().orElseThrow(()->new IllegalArgumentException("Quarto não encontrado."));
            data().adminCreateBooking(room,nome,cpf,LocalDate.parse(ent),LocalDate.parse(sai),n,pg,"Criada pelo funcionário "+Session.getName());
            atualizarTabelas();alerta("Reserva criada com sucesso.");
        }catch(Exception ex){alerta("Não foi possível criar a reserva: "+ex.getMessage());}
    }
    private void alterarStatusReserva(){
        HotelData.Booking b=reservaSelecionada();if(b==null)return;
        ChoiceDialog<String> d=new ChoiceDialog<>(b.getStatus(),java.util.Arrays.asList("PENDENTE","CONFIRMADA","CHECK_IN","CHECK_OUT","CANCELADA"));
        d.setTitle("Status da reserva");d.setHeaderText("Reserva #"+b.getId());d.setContentText("Novo status:");
        d.showAndWait().ifPresent(st->{try{data().adminSetBookingStatus(b.getId(),st);atualizarTabelas();alerta("Status atualizado.");}catch(Exception ex){alerta(ex.getMessage());}});
    }
    private void editarReservaFuncionario(){
        HotelData.Booking b=reservaSelecionada();if(b==null)return;
        try{
            String nome=texto("Editar reserva","Hóspede:",b.getGuestName());if(nome==null)return;
            String cpf=texto("Editar reserva","CPF:",b.getGuestCpf());if(cpf==null)return;
            String quarto=texto("Editar reserva","Número do quarto:",String.valueOf(b.getRoom().getNumber()));if(quarto==null)return;
            String entrada=texto("Editar reserva","Check-in (AAAA-MM-DD):",b.getCheckIn().toString());if(entrada==null)return;
            String saida=texto("Editar reserva","Check-out (AAAA-MM-DD):",b.getCheckOut().toString());if(saida==null)return;
            String qtd=texto("Editar reserva","Hóspedes:",String.valueOf(b.getGuests()));if(qtd==null)return;
            HotelData.Room r=data().getRooms().stream().filter(x->x.getNumber()==Integer.parseInt(quarto)).findFirst().orElseThrow();
            data().adminEditBooking(b.getId(),r,nome,cpf,LocalDate.parse(entrada),LocalDate.parse(saida),Integer.parseInt(qtd),b.getPaymentMethod(),b.getNotes());
            atualizarTabelas();alerta("Reserva editada.");
        }catch(Exception ex){alerta("Erro ao editar: "+ex.getMessage());}
    }
    private void novoQuarto(){
        try{int n=Integer.parseInt(texto("Novo quarto","Número:","501"));String cat=texto("Novo quarto","Categoria:","Standard");int cap=Integer.parseInt(texto("Novo quarto","Capacidade:","2"));double val=Double.parseDouble(texto("Novo quarto","Diária (ex. 199.90):","199.90"));String qual=texto("Novo quarto","Qualidade:","Confortável");String comod=texto("Novo quarto","Comodidades:","Wi-Fi, ar-condicionado");String tam=texto("Novo quarto","Tamanho:","20 m²");data().adminAddRoom(n,cat,cap,val,qual,comod,tam);atualizarTabelas();alerta("Quarto cadastrado.");}catch(Exception ex){alerta("Não foi possível cadastrar: "+ex.getMessage());}
    }
    private void editarQuarto(){
        HotelData.Room r=tabelaQuartos.getSelectionModel().getSelectedItem();if(r==null){alerta("Selecione um quarto.");return;}
        try{String cat=texto("Editar quarto","Categoria:",r.getCategory());if(cat==null)return;int cap=Integer.parseInt(texto("Editar quarto","Capacidade:",String.valueOf(r.getCapacity())));double val=Double.parseDouble(texto("Editar quarto","Diária:",r.getDailyRate().toString()));String qual=texto("Editar quarto","Qualidade:",r.getQuality());String comod=texto("Editar quarto","Comodidades:",r.getAmenities());String tam=texto("Editar quarto","Tamanho:",r.getSize());data().adminUpdateRoom(r,cat,cap,val,qual,comod,tam);atualizarTabelas();alerta("Quarto atualizado.");}catch(Exception ex){alerta("Erro: "+ex.getMessage());}
    }
    private void excluirQuarto(){HotelData.Room r=tabelaQuartos.getSelectionModel().getSelectedItem();if(r==null){alerta("Selecione um quarto.");return;}Alert a=new Alert(Alert.AlertType.CONFIRMATION,"Excluir o quarto "+r.getNumber()+"?",ButtonType.YES,ButtonType.NO);a.showAndWait().filter(x->x==ButtonType.YES).ifPresent(x->{try{data().adminDeleteRoom(r);atualizarTabelas();alerta("Quarto excluído.");}catch(Exception ex){alerta(ex.getMessage());}});}
    private void statusQuarto(){HotelData.Room r=tabelaQuartos.getSelectionModel().getSelectedItem();if(r==null){alerta("Selecione um quarto.");return;}ChoiceDialog<String>d=new ChoiceDialog<>(r.getAdminStatus(),java.util.Arrays.asList("ATIVO","MANUTENCAO","BLOQUEADO"));d.setHeaderText("Status do quarto "+r.getNumber());d.showAndWait().ifPresent(st->{try{data().adminSetRoomStatus(r,st);atualizarTabelas();alerta("Status atualizado.");}catch(Exception ex){alerta(ex.getMessage());}});}
    private void registrarPagamento(){
        HotelData.Booking b=reservaSelecionada();if(b==null)return;
        try{String valor=texto("Registrar pagamento","Valor recebido:",b.getTotal().toString());if(valor==null)return;String forma=texto("Registrar pagamento","Forma (PIX, DINHEIRO, CREDITO, DEBITO):","PIX");if(forma==null)return;data().adminRegisterPayment(b.getId(),new BigDecimal(valor.replace(",",".")),forma);alerta("Pagamento registrado e salvo.");}catch(Exception ex){alerta("Erro no pagamento: "+ex.getMessage());}
    }
    private void editarHospedeSelecionado(){
        HotelData.Booking b=tabelaHospedes.getSelectionModel().getSelectedItem();
        if(b==null){alerta("Selecione um hóspede/hospedagem na tabela.");return;}
        try{
            String nome=texto("Editar hóspede","Nome:",b.getGuestName());if(nome==null)return;
            String cpf=texto("Editar hóspede","CPF:",b.getGuestCpf());if(cpf==null)return;
            data().adminEditBooking(b.getId(),b.getRoom(),nome,cpf,b.getCheckIn(),b.getCheckOut(),b.getGuests(),b.getPaymentMethod(),b.getNotes());
            atualizarTabelas();alerta("Dados do hóspede atualizados na reserva selecionada.");
        }catch(Exception ex){alerta("Não foi possível editar: "+ex.getMessage());}
    }
    private void atualizarTabelas(){
        if(tabelaReservas!=null)tabelaReservas.setItems(FXCollections.observableArrayList(data().getBookings()));
        if(tabelaQuartos!=null)tabelaQuartos.setItems(FXCollections.observableArrayList(data().getRooms()));
        atualizarDashboard();
    }
}
