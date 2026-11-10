package com.hotel;

import com.hotel.service.HotelData;
import com.hotel.service.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClienteController {
    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(BR);

    @FXML private DatePicker dpEntrada, dpSaida;
    @FXML private Spinner<Integer> spHospedes;
    @FXML private ComboBox<String> cbCategoria, cbOrdenacao;
    @FXML private VBox listaQuartos, listaReservas;
    @FXML private Label lblBoasVindas, lblMensagem, lblResumo, lblTotalTopo, lblContagemReservas;
    @FXML private TextArea txtObservacoes;
    @FXML private ComboBox<String> cbPagamento;
    @FXML private CheckBox chkConcordo;
    @FXML private Button btnConfirmar;

    private HotelData.Room quartoSelecionado;
    private long noitesSelecionadas;

    @FXML public void initialize() {
        String nome = Session.getName();
        lblBoasVindas.setText("Olá, " + (nome == null || nome.isBlank() ? "hóspede" : nome));
        LocalDate hoje = LocalDate.now();
        dpEntrada.setValue(hoje.plusDays(1));
        dpSaida.setValue(hoje.plusDays(3));
        configurarCalendarios();
        spHospedes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 2));
        cbCategoria.getItems().setAll("Todas as categorias", "Standard", "Superior", "Família", "Suíte");
        cbCategoria.setValue("Todas as categorias");
        cbOrdenacao.getItems().setAll("Menor preço", "Maior preço", "Maior capacidade");
        cbOrdenacao.setValue("Menor preço");
        cbPagamento.getItems().setAll("Pagamento na recepção", "PIX na recepção", "Cartão na recepção");
        cbPagamento.setValue("Pagamento na recepção");
        dpEntrada.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && dpSaida.getValue() != null && !dpSaida.getValue().isAfter(newV)) dpSaida.setValue(newV.plusDays(1));
            limparSelecao();
        });
        dpSaida.valueProperty().addListener((obs, oldV, newV) -> limparSelecao());
        spHospedes.valueProperty().addListener((obs, oldV, newV) -> limparSelecao());
        buscarQuartos();
        atualizarMinhasReservas();
    }

    private void configurarCalendarios() {
        dpEntrada.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        dpSaida.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate entrada = dpEntrada.getValue();
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)) || (entrada != null && !date.isAfter(entrada)));
            }
        });
    }

    @FXML private void buscarQuartos() {
        listaQuartos.getChildren().clear();
        limparSelecao();
        LocalDate entrada = dpEntrada.getValue(), saida = dpSaida.getValue();
        if (entrada == null || saida == null || entrada.isBefore(LocalDate.now()) || !saida.isAfter(entrada)) {
            mensagem("Escolha um check-in válido e um check-out posterior.", true);
            return;
        }
        int hospedes = spHospedes.getValue();
        String categoria = cbCategoria.getValue();
        List<HotelData.Room> quartos = HotelData.getInstance().getRooms().stream()
                .filter(room -> room.getCapacity() >= hospedes)
                .filter(room -> categoria == null || categoria.equals("Todas as categorias") || room.getCategory().equals(categoria))
                .filter(room -> HotelData.getInstance().isAvailable(room, entrada, saida))
                .sorted(ordenacao())
                .collect(Collectors.toList());

        if (quartos.isEmpty()) {
            Label vazio = new Label("Não encontramos quartos disponíveis com esses filtros. Tente outras datas ou uma categoria diferente.");
            vazio.getStyleClass().add("empty-state"); vazio.setWrapText(true);
            listaQuartos.getChildren().add(vazio);
            mensagem("Nenhum quarto disponível para esse período.", false);
            return;
        }
        for (HotelData.Room room : quartos) listaQuartos.getChildren().add(criarCardQuarto(room, entrada, saida, hospedes));
        mensagem(quartos.size() + (quartos.size() == 1 ? " opção encontrada." : " opções encontradas.") + " Selecione seu quarto favorito.", false);
    }

    private Comparator<HotelData.Room> ordenacao() {
        String sort = cbOrdenacao.getValue();
        if ("Maior preço".equals(sort)) return Comparator.comparing(HotelData.Room::getDailyRate).reversed();
        if ("Maior capacidade".equals(sort)) return Comparator.comparingInt(HotelData.Room::getCapacity).reversed();
        return Comparator.comparing(HotelData.Room::getDailyRate);
    }

    // ---- Fotos dos quartos -------------------------------------------------------------------
    // Coloque as imagens em: src/main/resources/com/example/hotel_atlas/imagens/quartos/
    // com o nome quarto-<número>.jpg  (ex.: quarto-401.jpg). Aceita .jpg, .jpeg e .png.
    // Sem arquivo, o card mostra um espaço reservado com o nome exato esperado.
    private static final String PASTA_FOTOS = "/com/example/hotel_atlas/imagens/quartos/";
    private static final String[] EXTENSOES_FOTO = {"jpg", "jpeg", "png"};
    private static final double FOTO_W = 260, FOTO_H = 180;
    private final Map<Integer, Image> cacheFotos = new HashMap<>();

    private Image carregarFotoQuarto(int numero) {
        if (cacheFotos.containsKey(numero)) return cacheFotos.get(numero);
        Image encontrada = null;
        for (String ext : EXTENSOES_FOTO) {
            try (InputStream in = ClienteController.class.getResourceAsStream(PASTA_FOTOS + "quarto-" + numero + "." + ext)) {
                if (in == null) continue;
                Image img = new Image(in);
                if (!img.isError()) { encontrada = img; break; }
            } catch (Exception ignored) { }
        }
        cacheFotos.put(numero, encontrada);
        return encontrada;
    }

    /** Espaço da foto do quarto: mostra a imagem (cortada para preencher) ou um espaço reservado. */
    private Node criarFotoQuarto(HotelData.Room room) {
        StackPane slot = new StackPane();
        slot.getStyleClass().add("room-photo");
        slot.setMinSize(FOTO_W, FOTO_H); slot.setPrefSize(FOTO_W, FOTO_H); slot.setMaxSize(FOTO_W, FOTO_H);
        Rectangle recorte = new Rectangle(FOTO_W, FOTO_H); recorte.setArcWidth(20); recorte.setArcHeight(20);
        slot.setClip(recorte);
        Image img = carregarFotoQuarto(room.getNumber());
        if (img != null) {
            ImageView view = new ImageView(img);
            double alvo = FOTO_W / FOTO_H, w = img.getWidth(), h = img.getHeight();
            if (w / h > alvo) { double nw = h * alvo; view.setViewport(new Rectangle2D((w - nw) / 2, 0, nw, h)); }
            else { double nh = w / alvo; view.setViewport(new Rectangle2D(0, (h - nh) / 2, w, nh)); }
            view.setFitWidth(FOTO_W); view.setFitHeight(FOTO_H); view.setPreserveRatio(false); view.setSmooth(true);
            slot.getChildren().add(view);
        } else {
            Label aviso = new Label("FOTO DO QUARTO " + room.getNumber() + "\nColoque o arquivo\nquarto-" + room.getNumber() + ".jpg");
            aviso.getStyleClass().add("room-photo-text"); aviso.setWrapText(true);
            slot.getChildren().add(aviso);
        }
        return slot;
    }

    private HBox criarCardQuarto(HotelData.Room room, LocalDate entrada, LocalDate saida, int hospedes) {
        HBox card = new HBox(16); card.getStyleClass().add("room-card");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox info = new VBox(9); HBox.setHgrow(info, Priority.ALWAYS);
        HBox heading = new HBox(10); heading.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label title = new Label("Quarto " + room.getNumber()); title.getStyleClass().add("room-title");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label category = new Label(room.getCategory()); category.getStyleClass().add("room-category");
        heading.getChildren().addAll(title, spacer, category);
        Label quality = new Label(room.getQuality() + "  •  " + room.getSize() + "  •  até " + room.getCapacity() + " hóspedes");
        quality.getStyleClass().add("room-meta");
        Label amenities = new Label(room.getAmenities()); amenities.setWrapText(true); amenities.getStyleClass().add("room-description");
        long nights = ChronoUnit.DAYS.between(entrada, saida);
        BigDecimal total = room.getDailyRate().multiply(BigDecimal.valueOf(nights));
        HBox footer = new HBox(12); footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox priceBox = new VBox(2);
        Label daily = new Label(MONEY.format(room.getDailyRate()) + " / diária"); daily.getStyleClass().add("room-price");
        Label totalLabel = new Label("Total de " + nights + (nights == 1 ? " diária: " : " diárias: ") + MONEY.format(total)); totalLabel.getStyleClass().add("room-total");
        priceBox.getChildren().addAll(daily, totalLabel);
        Region push = new Region(); HBox.setHgrow(push, Priority.ALWAYS);
        Button select = new Button("Escolher quarto"); select.getStyleClass().add("btn-primary");
        select.setOnAction(e -> selecionar(room, entrada, saida, hospedes));
        footer.getChildren().addAll(priceBox, push, select);
        info.getChildren().addAll(heading, quality, amenities, new Separator(), footer);
        card.getChildren().addAll(criarFotoQuarto(room), info);
        return card;
    }

    private void selecionar(HotelData.Room room, LocalDate entrada, LocalDate saida, int hospedes) {
        quartoSelecionado = room;
        noitesSelecionadas = ChronoUnit.DAYS.between(entrada, saida);
        BigDecimal total = room.getDailyRate().multiply(BigDecimal.valueOf(noitesSelecionadas));
        lblResumo.setText("Quarto " + room.getNumber() + " • " + room.getCategory() + "\n" +
                "Check-in: " + entrada.format(DATE) + "   |   Check-out: " + saida.format(DATE) + "\n" +
                noitesSelecionadas + (noitesSelecionadas == 1 ? " diária" : " diárias") + " • " + hospedes + (hospedes == 1 ? " hóspede" : " hóspedes") + "\n" +
                "Diária: " + MONEY.format(room.getDailyRate()) + "\nTOTAL: " + MONEY.format(total));
        lblTotalTopo.setText(MONEY.format(total));
        btnConfirmar.setDisable(false);
        mensagem("Quarto selecionado! Confira os dados e confirme sua reserva.", false);
    }

    @FXML private void confirmarReserva() {
        if (quartoSelecionado == null) { mensagem("Selecione um quarto antes de confirmar.", true); return; }
        if (!chkConcordo.isSelected()) { mensagem("Confirme que revisou as datas e as condições da reserva.", true); return; }
        String cpf = Session.getCpf();
        try {
            HotelData.Booking booking = HotelData.getInstance().createBooking(quartoSelecionado,
                    Session.getName(), cpf, dpEntrada.getValue(), dpSaida.getValue(), spHospedes.getValue(),
                    cbPagamento.getValue(), txtObservacoes.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reserva realizada"); alert.setHeaderText("Tudo certo! Sua reserva foi registrada.");
            alert.setContentText("Código: #" + booking.getId() + "\nQuarto: " + booking.getRoom().getNumber() +
                    "\nPeríodo: " + booking.getCheckIn().format(DATE) + " a " + booking.getCheckOut().format(DATE) +
                    "\nTotal: " + booking.getTotalText() + "\nPagamento: " + booking.getPaymentMethod() +
                    "\n\nGuarde o código da reserva para consultar com a recepção.");
            alert.showAndWait();
            txtObservacoes.clear(); chkConcordo.setSelected(false);
            buscarQuartos(); atualizarMinhasReservas();
        } catch (RuntimeException ex) { mensagem(ex.getMessage(), true); }
    }

    @FXML private void atualizarMinhasReservas() {
        listaReservas.getChildren().clear();
        List<HotelData.Booking> reservas = HotelData.getInstance().getBookingsFor(Session.getCpf());
        lblContagemReservas.setText(reservas.size() + (reservas.size() == 1 ? " reserva" : " reservas"));
        if (reservas.isEmpty()) {
            Label empty = new Label("Você ainda não possui reservas. Quando fizer sua primeira reserva, ela aparecerá aqui.");
            empty.getStyleClass().add("empty-state"); empty.setWrapText(true); listaReservas.getChildren().add(empty); return;
        }
        for (HotelData.Booking booking : reservas) {
            VBox card = new VBox(8); card.getStyleClass().add("booking-card");
            HBox top = new HBox(10); top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label title = new Label("Reserva #" + booking.getId() + "  •  Quarto " + booking.getRoom().getNumber()); title.getStyleClass().add("room-title");
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Label status = new Label("CANCELADA".equals(booking.getStatus()) ? "Cancelada" : "Confirmada");
            status.getStyleClass().add("CANCELADA".equals(booking.getStatus()) ? "booking-cancelled" : "booking-confirmed");
            top.getChildren().addAll(title, spacer, status);
            Label dates = new Label(booking.getCheckIn().format(DATE) + " até " + booking.getCheckOut().format(DATE) +
                    "  •  " + booking.getGuests() + (booking.getGuests() == 1 ? " hóspede" : " hóspedes"));
            Label details = new Label(booking.getRoom().getCategory() + "  •  " + booking.getPaymentMethod() + "  •  " + booking.getTotalText());
            details.setWrapText(true); details.getStyleClass().add("room-meta");
            Button detailButton = new Button("Ver detalhes");
            detailButton.setOnAction(e -> mostrarDetalhes(booking));
            card.getChildren().addAll(top, dates, details, detailButton);
            if (booking.getNotes() != null && !booking.getNotes().isBlank()) {
                Label note = new Label("Observações: " + booking.getNotes()); note.setWrapText(true); card.getChildren().add(note);
            }
            if ("CONFIRMADA".equals(booking.getStatus()) && booking.getCheckIn().isAfter(LocalDate.now())) {
                Button cancel = new Button("Cancelar reserva"); cancel.getStyleClass().add("btn-danger");
                cancel.setOnAction(e -> cancelarReserva(booking.getId())); card.getChildren().add(cancel);
            }
            listaReservas.getChildren().add(card);
        }
    }

    private void mostrarDetalhes(HotelData.Booking booking) {
        Alert detalhes = new Alert(Alert.AlertType.INFORMATION);
        detalhes.setTitle("Detalhes da reserva #" + booking.getId());
        detalhes.setHeaderText("Quarto " + booking.getRoom().getNumber() + " — " + booking.getRoom().getCategory());
        detalhes.setContentText("Status: " + booking.getStatus() + "\n" +
                "Titular: " + booking.getGuestName() + "\n" +
                "CPF da conta: " + mascararCpf(booking.getGuestCpf()) + "\n" +
                "Check-in: " + booking.getCheckIn().format(DATE) + "\n" +
                "Check-out: " + booking.getCheckOut().format(DATE) + "\n" +
                "Hóspedes: " + booking.getGuests() + "\n" +
                "Pagamento: " + booking.getPaymentMethod() + "\n" +
                "Total: " + booking.getTotalText() + "\n" +
                "Observações: " + (booking.getNotes().isBlank() ? "Nenhuma" : booking.getNotes()));
        detalhes.showAndWait();
    }

    private String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "não disponível";
        return "***.***.***-" + cpf.substring(9);
    }

    private void cancelarReserva(long id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar reserva"); confirm.setHeaderText("Deseja cancelar a reserva #" + id + "?");
        confirm.setContentText("O quarto voltará a aparecer como disponível. Esta ação não pode ser desfeita.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean cancelled = HotelData.getInstance().cancelBooking(id, Session.getCpf());
            mensagem(cancelled ? "Reserva #" + id + " cancelada." : "Não foi possível cancelar. Verifique o status da reserva.", !cancelled);
            atualizarMinhasReservas(); buscarQuartos();
        }
    }

    private void limparSelecao() {
        quartoSelecionado = null; noitesSelecionadas = 0;
        if (lblResumo != null) lblResumo.setText("Selecione um quarto para visualizar o resumo completo.");
        if (lblTotalTopo != null) lblTotalTopo.setText("—");
        if (btnConfirmar != null) btnConfirmar.setDisable(true);
        if (chkConcordo != null) chkConcordo.setSelected(false);
    }

    private void mensagem(String texto, boolean erro) {
        lblMensagem.setText(texto == null ? "Ocorreu um erro inesperado." : texto);
        lblMensagem.getStyleClass().removeAll("error-text", "info-text");
        lblMensagem.getStyleClass().add(erro ? "error-text" : "info-text");
    }

    @FXML private void sair() {
        try {
            Session.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hotel_atlas/login-view.fxml"));
            Stage stage = (Stage) lblBoasVindas.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1280, 768));
            stage.setTitle("Hotel Atlas - Acesso");
        } catch (Exception e) { e.printStackTrace(); mensagem("Não foi possível voltar ao login.", true); }
    }
}
