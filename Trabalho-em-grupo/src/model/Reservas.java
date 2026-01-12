package model;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
/*
O que deve conter a classe Reservas?
atributos:
- idReserva
- idQuarto
- idCliente
- numeroHospedes
-dataCheckIn
-dataCheckOut
-ativo (boolean)

metodos:
- criar reserva(): encontrar qualquer quarto livre adequado, capacidade >= numeroHospedes, escolher o mais proximo possivel do n de hospedes
-listar reservas ativas()
-listar reservas por quarto ()(presentes ou futuras)
-listar reservas por hospede ()(presentes ou futuras)
-Editar reserva() (n de hospedes e/ou datas, revalidar capacidade e conflitos)
-cancelar reserva() (setar ativo para false)
*/

public class Reservas {
    public int idReserva;
    public int idQuarto;
    public int idHospede;
    public int numeroHospedes;
    public LocalDate dataInicio;
    public LocalDate dataFim;
    public boolean ativa;

    private static final int MAX_RESERVAS = 1000;
    private static final Reservas[] reservas = new Reservas[MAX_RESERVAS];
    private static int contadorReservas = 0;
    private static int proximoIdReserva = 1;

    private Reservas(int idQuarto, int idHospede, int numeroHospedes, LocalDate dataInicio, LocalDate dataFim) {
        this.idReserva = proximoIdReserva++;
        this.idQuarto = idQuarto;
        this.idHospede = idHospede;
        this.numeroHospedes = numeroHospedes;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativa = true;
    }

    /**
     * criar reserva se encontrar "qualquer quarto livre adequado" e escolhendo a
     * capacidade mais próxima (mínima) >= numeroHospedes.
     * retorna true se ja estiver criada; false caso falhe nao
     */

    public static boolean criarReservaAutoQuarto(int idHospede, int numeroHospedes, String dataInicioStr,
            String dataFimStr) {

        if (contadorReservas >= reservas.length) {
            System.out.println("Não foi possível criar reserva: limite de reservas atingido.");
            return false;
        }

        if (!validarNumeroHospedes(numeroHospedes)) {
            return false;
        }

        LocalDate di = parseData(dataInicioStr);
        LocalDate df = parseData(dataFimStr);
        if (di == null || df == null) {
            return false;
        }

        if (!validarIntervaloDatas(di, df)) {
            return false;
        }

        if (!existeHospede(idHospede)) {
            System.out.println("Não foi possível criar reserva: hóspede com ID " + idHospede + " não existe.");
            return false;
        }

        Quarto melhor = null;
        int melhorCap = Integer.MAX_VALUE;

        int totalQuartos = Quarto.getContadorQuartos();
        for (int i = 0; i < totalQuartos; i++) {
            Quarto q = Quarto.getQuartoPorIndex(i);
            if (q == null) {
                continue;
            }

            if (q.capacidade >= numeroHospedes) {
                boolean livre = !temConflitoAtivoNoQuarto(q.idQuarto, -1, di, df);
                if (livre && q.capacidade < melhorCap) {
                    melhor = q;
                    melhorCap = q.capacidade;
                }
            }
        }

        if (melhor == null) {
            System.out.println("Não foi possível criar reserva: não há quartos disponíveis com capacidade para "
                    + numeroHospedes + " hóspedes nesse período.");
            return false;
        }

        Reservas r = new Reservas(melhor.idQuarto, idHospede, numeroHospedes, di, df);
        reservas[contadorReservas++] = r;

        atualizarEstadoOcupacaoQuartos();

        System.out.println("Reserva criada com sucesso! ID Reserva: " + r.idReserva + " | Quarto ID: " + r.idQuarto);
        return true;
    }

    public static void listarTodasAsReservas() {
        if (contadorReservas == 0) {
            System.out.println("Nenhuma reserva cadastrada.");
            return;
        }

        System.out.println("Lista de reservas:");
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null) {
                continue;
            }
            System.out.println(formatarReservaLinha(r));
        }
    }

    /** reservas presentes ou futuras */
    public static void listarReservasPorQuarto(int idQuarto) {
        LocalDate hoje = LocalDate.now();
        boolean encontrou = false;

        System.out.println("Reservas (presentes/futuras) do quarto ID " + idQuarto + ":");
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null || !r.ativa) {
                continue;
            }
            if (r.idQuarto != idQuarto) {
                continue;
            }
            if (r.dataFim.isBefore(hoje)) {
                continue;
            }

            System.out.println(formatarReservaLinha(r));
            encontrou = true;
        }

        if (!encontrou) {
            System.out.println("Nenhuma reserva presente/futura encontrada para este quarto.");
        }
    }

    /** reservas presentes ou futuras */
    public static void listarReservasPorHospede(int idHospede) {
        LocalDate hoje = LocalDate.now();
        boolean encontrou = false;

        System.out.println("Reservas (presentes/futuras) do hóspede ID " + idHospede + ":");
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null || !r.ativa) {
                continue;
            }
            if (r.idHospede != idHospede) {
                continue;
            }
            if (r.dataFim.isBefore(hoje)) {
                continue;
            }

            System.out.println(formatarReservaLinha(r));
            encontrou = true;
        }

        if (!encontrou) {
            System.out.println("Nenhuma reserva presente/futura encontrada para este hóspede.");
        }
    }

    /**
     * editar reserva: pode mudar nº hóspedes e/ou datas rever a capacidade dos quartos
     */
    public static boolean editarReserva(int idReserva, Integer novoNumeroHospedes, String novaDataInicioStr,
            String novaDataFimStr) {

        Reservas r = procurarReservaPorId(idReserva);
        if (r == null) {
            System.out.println("Reserva com ID " + idReserva + " não encontrada.");
            return false;
        }
        if (!r.ativa) {
            System.out.println("Não é possível editar: reserva está cancelada/inativa.");
            return false;
        }

        int numHosp = (novoNumeroHospedes != null) ? novoNumeroHospedes : r.numeroHospedes;
        if (!validarNumeroHospedes(numHosp)) {
            return false;
        }

        LocalDate di = (novaDataInicioStr != null && !novaDataInicioStr.isBlank()) ? parseData(novaDataInicioStr)
                : r.dataInicio;
        LocalDate df = (novaDataFimStr != null && !novaDataFimStr.isBlank()) ? parseData(novaDataFimStr) : r.dataFim;

        if (di == null || df == null) {
            return false;
        }
        if (!validarIntervaloDatas(di, df)) {
            return false;
        }

        Quarto q = Quarto.getQuartoPorId(r.idQuarto);
        if (q == null) {
            System.out.println("Não foi possível editar: quarto da reserva não existe.");
            return false;
        }

        if (numHosp > q.capacidade) {
            System.out.println("Não foi possível editar: nº hóspedes (" + numHosp + ") excede a capacidade do quarto ("
                    + q.capacidade + ").");
            return false;
        }

        if (temConflitoAtivoNoQuarto(r.idQuarto, r.idReserva, di, df)) {
            System.out.println(
                    "Não foi possível editar: as novas datas entram em conflito com outra reserva ativa do mesmo quarto.");
            return false;
        }

        r.numeroHospedes = numHosp;
        r.dataInicio = di;
        r.dataFim = df;

        atualizarEstadoOcupacaoQuartos();

        System.out.println("Reserva " + idReserva + " editada com sucesso.");
        return true;
    }

    public static boolean cancelarReserva(int idReserva) {
        Reservas r = procurarReservaPorId(idReserva);
        if (r == null) {
            System.out.println("Reserva com ID " + idReserva + " não encontrada.");
            return false;
        }
        if (!r.ativa) {
            System.out.println("Reserva já se encontra cancelada/inativa.");
            return false;
        }

        r.ativa = false;
        atualizarEstadoOcupacaoQuartos();

        System.out.println("Reserva " + idReserva + " cancelada com sucesso.");
        return true;
    }

    /**
     * Atualiza quarto com base nas reservas ativas que incluem o dia de hoje.
     */
    public static void atualizarEstadoOcupacaoQuartos() {
        LocalDate hoje = LocalDate.now();

        int totalQuartos = Quarto.getContadorQuartos();
        for (int i = 0; i < totalQuartos; i++) {
            Quarto q = Quarto.getQuartoPorIndex(i);
            if (q == null) {
                continue;
            }
            q.estaOcupado = false;
        }

        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null || !r.ativa) {
                continue;
            }

            boolean vigente = !hoje.isBefore(r.dataInicio) && !hoje.isAfter(r.dataFim);
            if (vigente) {
                Quarto q = Quarto.getQuartoPorId(r.idQuarto);
                if (q != null) {
                    q.estaOcupado = true;
                }
            }
        }
    }

   

    // validações

    private static Reservas procurarReservaPorId(int idReserva) {
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r != null && r.idReserva == idReserva) {
                return r;
            }
        }
        return null;
    }

    private static boolean validarNumeroHospedes(int numeroHospedes) {
        if (numeroHospedes < 1) {
            System.out.println("Número de hóspedes inválido. Deve ser >= 1.");
            return false;
        }
        return true;
    }

    private static boolean validarIntervaloDatas(LocalDate di, LocalDate df) {
        if (di.isAfter(df)) {
            System.out.println("Datas inválidas: dataInicio não pode ser depois de dataFim.");
            return false;
        }
        return true;
    }

    private static LocalDate parseData(String s) {
        if (s == null || s.isBlank()) {
            System.out.println("Data inválida: valor vazio.");
            return null;
        }
        try {
            return LocalDate.parse(s); // YYYY-MM-DD
        } catch (DateTimeParseException ex) {
            System.out.println("Data inválida: '" + s + "'. Use o formato YYYY-MM-DD.");
            return null;
        }
    }

    /**
     * Ignora reserva com idReserva==idIgnorar 
     */
    private static boolean temConflitoAtivoNoQuarto(int idQuarto, int idIgnorar, LocalDate di, LocalDate df) {
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null || !r.ativa) {
                continue;
            }
            if (r.idQuarto != idQuarto) {
                continue;
            }
            if (r.idReserva == idIgnorar) {
                continue;
            }

            if (intervalosIntersectam(di, df, r.dataInicio, r.dataFim)) {
                return true;
            }
        }
        return false;
    }

    private static boolean intervalosIntersectam(LocalDate aIni, LocalDate aFim, LocalDate bIni, LocalDate bFim) {
        return !aIni.isAfter(bFim) && !bIni.isAfter(aFim);
    }

    private static String formatarReservaLinha(Reservas r) {
        return "Reserva ID: " + r.idReserva +
                " | Quarto ID: " + r.idQuarto +
                " | Hóspede ID: " + r.idHospede +
                " | Nº hóspedes: " + r.numeroHospedes +
                " | Início: " + r.dataInicio +
                " | Fim: " + r.dataFim +
                " | Ativa: " + (r.ativa ? "Sim" : "Não");
    }

    private static boolean existeHospede(int idHospede) {
        try {
            Method m = Hospede.class.getMethod("getHospedePorId", int.class);
            Object result = m.invoke(null, idHospede);
            return result != null;
        } catch (ReflectiveOperationException e) {
         
            return false;
        }
    }


     /**
     * Retorna a reserva ativa vigente (hoje entre dataInicio e dataFim, inclusivo)
     * para um quarto. Se houver mais de uma (não deveria ocorrer), retorna a primeira encontrada.
     */
    public static Reservas getReservaAtivaVigentePorQuarto(int idQuarto) {
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < contadorReservas; i++) {
            Reservas r = reservas[i];
            if (r == null || !r.ativa) {
                continue;
            }
            if (r.idQuarto != idQuarto) {
                continue;
            }
            boolean vigente = !hoje.isBefore(r.dataInicio) && !hoje.isAfter(r.dataFim);
            if (vigente) {
                return r;
            }
        }
        return null;
    }

    /**
     * Formata informações de ocupação de um quarto (hóspede + check-in/out) com base
     * na reserva vigente.
     */
    public static String formatarDadosOcupacao(Reservas r) {
        if (r == null) {
            return "(sem dados de ocupação)";
        }
        Hospede h = Hospede.getHospedePorId(r.idHospede);
        String nome = (h != null && h.nome != null) ? h.nome : "(hóspede desconhecido)";
        return "Hóspede: " + nome +
                " | Check-in: " + r.dataInicio +
                " | Check-out: " + r.dataFim;
    }
}