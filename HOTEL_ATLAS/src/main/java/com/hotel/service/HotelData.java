package com.hotel.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** Catálogo de quartos e repositório local persistente de reservas (JSON). */
public final class HotelData {
    // Inicialize dependências e caminhos ANTES do singleton: o construtor carrega o JSON.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type RECORDS_TYPE = new TypeToken<List<BookingRecord>>(){}.getType();
    private static final Path DATA_DIR = resolveDataDirectory();
    private static final Path FILE = DATA_DIR.resolve("reservas.json");
    private static final Path ROOMS_FILE = DATA_DIR.resolve("quartos.json");
    private static final Path PAYMENTS_FILE = DATA_DIR.resolve("pagamentos.json");
    private static final HotelData INSTANCE = new HotelData();

    private static Path resolveDataDirectory() {
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) {
            home = System.getProperty("user.dir", ".");
        }
        return Path.of(home).toAbsolutePath().normalize().resolve(".hotel-atlas");
    }
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private long nextBookingId = 1001;

    private HotelData() {
        rooms.add(new Room(101,"Standard",2,189.90,"Confortável","Wi-Fi, ar-condicionado, TV e banheiro privativo","18 m²"));
        rooms.add(new Room(102,"Standard",2,199.90,"Confortável","Wi-Fi, ar-condicionado, TV e banheiro privativo","20 m²"));
        rooms.add(new Room(201,"Superior",3,279.90,"Premium","Cama queen, frigobar, mesa de trabalho e vista da cidade","26 m²"));
        rooms.add(new Room(202,"Superior",3,299.90,"Premium","Cama queen, frigobar, varanda e vista da cidade","29 m²"));
        rooms.add(new Room(301,"Família",4,369.90,"Alto conforto","Duas camas, sala compacta, frigobar e TV","35 m²"));
        rooms.add(new Room(302,"Família",5,399.90,"Alto conforto","Duas camas, varanda, sala e frigobar","40 m²"));
        rooms.add(new Room(401,"Suíte",2,499.90,"Luxo","Cama king, banheira, minibar e vista panorâmica","45 m²"));
        rooms.add(new Room(402,"Suíte",2,549.90,"Luxo","Cama king, banheira e varanda privativa","52 m²"));
        carregarQuartos(); carregarReservas(); carregarPagamentos();
    }
    public static HotelData getInstance(){ return INSTANCE; }
    public List<Room> getRooms(){ return Collections.unmodifiableList(rooms); }
    public synchronized List<Booking> getBookings(){ return Collections.unmodifiableList(new ArrayList<>(bookings)); }
    /** Cliente só recebe reservas associadas ao CPF autenticado. */
    public synchronized List<Booking> getBookingsFor(String cpf){
        String key = digits(cpf);
        return bookings.stream().filter(b -> key.equals(b.getGuestCpf()))
                .sorted(Comparator.comparing(Booking::getCheckIn).reversed()).collect(Collectors.toList());
    }
    public synchronized boolean isAvailable(Room room, LocalDate in, LocalDate out){
        if(room==null||in==null||out==null||!out.isAfter(in)||room.adminStatus!=StatusRoom.ATIVO) return false;
        return bookings.stream().noneMatch(b -> b.getRoom().getNumber()==room.getNumber()
                && !"CANCELADA".equals(b.getStatus()) && in.isBefore(b.getCheckOut()) && out.isAfter(b.getCheckIn()));
    }
    public synchronized Booking createBooking(Room room, String guestName, String cpf, LocalDate in, LocalDate out,
                                               int guests, String payment, String notes){
        String normalizedCpf = digits(cpf);
        if(room==null||!rooms.contains(room)) throw new IllegalArgumentException("Selecione um quarto válido.");
        if(guestName==null||guestName.trim().length()<2) throw new IllegalArgumentException("Nome do cliente não informado.");
        if(!normalizedCpf.matches("\\d{11}")) throw new IllegalArgumentException("Sessão inválida. Entre novamente com seu CPF.");
        if(in==null||out==null||in.isBefore(LocalDate.now())||!out.isAfter(in)) throw new IllegalArgumentException("Confira as datas da hospedagem.");
        if(guests<1||guests>room.getCapacity()) throw new IllegalArgumentException("A capacidade deste quarto é de até "+room.getCapacity()+" hóspede(s).");
        if(payment==null||payment.isBlank()) throw new IllegalArgumentException("Selecione uma forma de pagamento.");
        if(notes!=null&&notes.trim().length()>500) throw new IllegalArgumentException("As observações devem ter no máximo 500 caracteres.");
        if(!isAvailable(room,in,out)) throw new IllegalStateException("Quarto indisponível para o período escolhido. Faça uma nova busca.");
        long nights=ChronoUnit.DAYS.between(in,out);
        Booking b=new Booking(nextBookingId++,room,guestName.trim(),normalizedCpf,in,out,guests,
                room.getDailyRate().multiply(BigDecimal.valueOf(nights)),"CONFIRMADA",payment,notes==null?"":notes.trim());
        bookings.add(b); salvarReservas(); return b;
    }
    public synchronized boolean cancelBooking(long id,String cpf){
        String key=digits(cpf);
        for(Booking b:bookings) if(b.getId()==id&&key.equals(b.getGuestCpf())&&"CONFIRMADA".equals(b.getStatus())&&b.getCheckIn().isAfter(LocalDate.now())){
            b.cancel(); salvarReservas(); return true;
        }
        return false;
    }

    public enum StatusRoom { ATIVO, MANUTENCAO, BLOQUEADO }
    private final List<PaymentRecord> payments=new ArrayList<>(); private long nextPaymentId=1;
    public synchronized Booking findBooking(long id){return bookings.stream().filter(b->b.getId()==id).findFirst().orElse(null);}
    public synchronized void adminSetBookingStatus(long id,String status){Booking b=findBooking(id);if(b==null)throw new IllegalArgumentException("Reserva não encontrada.");if(!Set.of("PENDENTE","CONFIRMADA","CHECK_IN","CHECK_OUT","CANCELADA").contains(status))throw new IllegalArgumentException("Status inválido.");b.setStatus(status);salvarReservas();}
    public synchronized Booking adminCreateBooking(Room room,String name,String cpf,LocalDate in,LocalDate out,int guests,String payment,String notes){return createBooking(room,name,cpf,in,out,guests,payment,notes);}
    public synchronized void adminEditBooking(long id,Room room,String name,String cpf,LocalDate in,LocalDate out,int guests,String payment,String notes){
        Booking b=findBooking(id);if(b==null)throw new IllegalArgumentException("Reserva não encontrada.");
        if(room==null||name==null||name.isBlank()||in==null||out==null||!out.isAfter(in)||guests<1||guests>room.getCapacity())throw new IllegalArgumentException("Confira os dados da reserva.");
        boolean conflict=bookings.stream().anyMatch(x->x.getId()!=id&&x.getRoom().getNumber()==room.getNumber()&&!"CANCELADA".equals(x.getStatus())&&in.isBefore(x.getCheckOut())&&out.isAfter(x.getCheckIn()));
        if(conflict)throw new IllegalStateException("Quarto indisponível nesse período.");b.edit(room,name,cpf,in,out,guests,payment,notes);salvarReservas();
    }
    public synchronized Room adminAddRoom(int number,String category,int capacity,double rate,String quality,String amenities,String size){if(rooms.stream().anyMatch(r->r.getNumber()==number))throw new IllegalArgumentException("Número de quarto já cadastrado.");Room r=new Room(number,category,capacity,rate,quality,amenities,size);rooms.add(r);salvarReservas();return r;}
    public synchronized void adminUpdateRoom(Room room,String category,int capacity,double rate,String quality,String amenities,String size){if(room==null||capacity<1||rate<=0)throw new IllegalArgumentException("Dados inválidos.");room.update(category,capacity,rate,quality,amenities,size);salvarReservas();}
    public synchronized void adminDeleteRoom(Room room){if(room==null)throw new IllegalArgumentException("Selecione um quarto.");if(bookings.stream().anyMatch(b->b.getRoom().getNumber()==room.getNumber()&&!Set.of("CANCELADA","CHECK_OUT").contains(b.getStatus())))throw new IllegalStateException("Quarto possui hospedagem ou reserva ativa.");rooms.remove(room);salvarReservas();}
    public synchronized void adminSetRoomStatus(Room room,String status){if(room==null)throw new IllegalArgumentException("Selecione um quarto.");room.setAdminStatus(status);salvarReservas();}
    public synchronized void adminRegisterPayment(long bookingId,BigDecimal amount,String method){if(findBooking(bookingId)==null||amount==null||amount.signum()<=0||method==null||method.isBlank())throw new IllegalArgumentException("Informe reserva, valor e forma de pagamento.");payments.add(new PaymentRecord(nextPaymentId++,bookingId,amount.toPlainString(),method,LocalDate.now().toString()));salvarReservas();}
    public synchronized List<PaymentRecord> getPayments(){return Collections.unmodifiableList(new ArrayList<>(payments));}
    public static class PaymentRecord {public long id,bookingId;public String amount,method,date;public PaymentRecord(){} PaymentRecord(long i,long b,String a,String m,String d){id=i;bookingId=b;amount=a;method=m;date=d;}public long getId(){return id;}public long getBookingId(){return bookingId;}public String getAmount(){return amount;}public String getMethod(){return method;}public String getDate(){return date;}}

    public String getCaminhoArquivoReservas(){ return FILE.toString(); }
    private void carregarReservas(){
        if(!Files.exists(FILE)) return;
        try {
            List<BookingRecord> records=GSON.fromJson(Files.readString(FILE,StandardCharsets.UTF_8),RECORDS_TYPE);
            if(records==null)return;
            for(BookingRecord r:records){
                Room room=rooms.stream().filter(x->x.getNumber()==r.roomNumber).findFirst().orElse(null);
                if(room==null||r.cpf==null||r.checkIn==null||r.checkOut==null)continue;
                bookings.add(new Booking(r.id,room,r.guestName,r.cpf,LocalDate.parse(r.checkIn),LocalDate.parse(r.checkOut),
                        r.guests,new BigDecimal(r.total),r.status,r.paymentMethod,r.notes));
                nextBookingId=Math.max(nextBookingId,r.id+1);
            }
        } catch(Exception e){ throw new IllegalStateException("Não foi possível carregar reservas em "+FILE+". Faça uma cópia do arquivo antes de tentar repará-lo.",e); }
    }
    private void carregarQuartos(){try{if(Files.exists(ROOMS_FILE)){List<RoomRecord> rs=GSON.fromJson(Files.readString(ROOMS_FILE),new TypeToken<List<RoomRecord>>(){}.getType());if(rs!=null)for(RoomRecord r:rs){Room old=rooms.stream().filter(x->x.getNumber()==r.number).findFirst().orElse(null);if(old!=null){old.update(r.category,r.capacity,r.rate,r.quality,r.amenities,r.size);old.setAdminStatus(r.status);}else{Room added=new Room(r.number,r.category,r.capacity,r.rate,r.quality,r.amenities,r.size);added.setAdminStatus(r.status);rooms.add(added);}}}}catch(Exception e){throw new IllegalStateException("Erro ao carregar quartos.",e);}}
    private void carregarPagamentos(){try{if(Files.exists(PAYMENTS_FILE)){List<PaymentRecord> ps=GSON.fromJson(Files.readString(PAYMENTS_FILE),new TypeToken<List<PaymentRecord>>(){}.getType());if(ps!=null){payments.addAll(ps);for(PaymentRecord x:ps)nextPaymentId=Math.max(nextPaymentId,x.id+1);}}}catch(Exception e){throw new IllegalStateException("Erro ao carregar pagamentos.",e);}}
    private void salvarReservas(){
        try{
            Files.createDirectories(FILE.getParent());
            List<BookingRecord> records=bookings.stream().map(BookingRecord::new).toList();
            Path tmp=FILE.resolveSibling(FILE.getFileName()+".tmp");
            Files.writeString(tmp,GSON.toJson(records),StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            try{Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING);}
        }catch(IOException e){throw new IllegalStateException("Não foi possível salvar as reservas no computador.",e);} salvarAuxiliares();
    }
    private void salvarAuxiliares(){try{Files.createDirectories(DATA_DIR);Files.writeString(ROOMS_FILE,GSON.toJson(rooms.stream().map(RoomRecord::new).toList()),StandardCharsets.UTF_8);Files.writeString(PAYMENTS_FILE,GSON.toJson(payments),StandardCharsets.UTF_8);}catch(IOException e){throw new IllegalStateException("Não foi possível salvar quartos/pagamentos.",e);}}
    private static class RoomRecord {int number,capacity;String category,quality,amenities,size,status;double rate;RoomRecord(Room r){number=r.number;capacity=r.capacity;category=r.category;quality=r.quality;amenities=r.amenities;size=r.size;rate=r.dailyRate.doubleValue();status=r.getAdminStatus();}}
    private static String digits(String s){return s==null?"":s.replaceAll("\\D","");}

    private static class BookingRecord{
        long id; int roomNumber; String guestName; String cpf; String checkIn; String checkOut; int guests; String total; String status; String paymentMethod; String notes;
        BookingRecord(Booking b){id=b.id;roomNumber=b.room.getNumber();guestName=b.guestName;cpf=b.guestCpf;checkIn=b.checkIn.toString();checkOut=b.checkOut.toString();guests=b.guests;total=b.total.toPlainString();status=b.status;paymentMethod=b.paymentMethod;notes=b.notes;}
    }
    public static class Room{
        private int number,capacity; private String category,quality,amenities,size; private BigDecimal dailyRate; private StatusRoom adminStatus = StatusRoom.ATIVO;
        public Room(int n,String c,int cap,double rate,String q,String a,String s){number=n;category=c;capacity=cap;dailyRate=BigDecimal.valueOf(rate);quality=q;amenities=a;size=s;}
        public void update(String c,int cap,double rate,String q,String a,String s){category=c;capacity=cap;dailyRate=BigDecimal.valueOf(rate);quality=q;amenities=a;size=s;} public String getAdminStatus(){return adminStatus.name();} public void setAdminStatus(String status){adminStatus=StatusRoom.valueOf(status);} public int getNumber(){return number;} public String getCategory(){return category;} public int getCapacity(){return capacity;} public BigDecimal getDailyRate(){return dailyRate;} public String getQuality(){return quality;} public String getAmenities(){return amenities;} public String getSize(){return size;}
        public String getRateText(){return String.format(new Locale("pt","BR"),"R$ %,.2f",dailyRate);}
        @Override public String toString(){return "Quarto "+number+" - "+category;}
    }
    public static class Booking{
        private final long id; private Room room; private String guestName,guestCpf; private LocalDate checkIn,checkOut; private int guests; private BigDecimal total; private String status; private String paymentMethod,notes;
        public Booking(long id,Room room,String name,String cpf,LocalDate in,LocalDate out,int guests,BigDecimal total,String status,String payment,String notes){this.id=id;this.room=room;guestName=name;guestCpf=digits(cpf);checkIn=in;checkOut=out;this.guests=guests;this.total=total;this.status=status;paymentMethod=payment;this.notes=notes;}
        private void cancel(){status="CANCELADA";} private void setStatus(String value){status=value;} private void edit(Room r,String name,String cpf,LocalDate in,LocalDate out,int qtd,String pay,String obs){room=r;guestName=name;guestCpf=digits(cpf);checkIn=in;checkOut=out;guests=qtd;paymentMethod=pay;notes=obs;total=r.getDailyRate().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(in,out)));}
        public long getId(){return id;} public Room getRoom(){return room;} public String getGuestName(){return guestName;} public String getGuestCpf(){return guestCpf;} public String getGuestEmail(){return guestCpf;}
        public LocalDate getCheckIn(){return checkIn;} public LocalDate getCheckOut(){return checkOut;} public int getGuests(){return guests;} public BigDecimal getTotal(){return total;}
        public String getTotalText(){return String.format(new Locale("pt","BR"),"R$ %,.2f",total);} public String getStatus(){return status;} public String getPaymentMethod(){return paymentMethod;} public String getNotes(){return notes;}
    }
}
