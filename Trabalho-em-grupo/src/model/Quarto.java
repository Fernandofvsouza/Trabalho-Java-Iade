package model;


/*
Requisitos da class quarto:
Atributos:
- idQuarto
- numeroQuarto
- capacidade
-estaOcupado

Metodos:
listarTodosOsQuartos()
listarQuartosDisponiveis()
ListarQuartosOcupados() (mostrar dados de ocupação do quarto)
ListarQuartoEspecifico() (Mostar todas as reservas do quarto)
*/
public class Quarto {
    int idQuarto;
    int numeroQuarto;
    int capacidade;
    boolean estaOcupado;

    // Array dos quartos

    private static Quarto[] quartos = new Quarto[10];
    private static int contadorQuartos = 0;

    // Metodo construtor
    public Quarto(int idQuarto, int numeroQuarto, int capacidade, boolean estaOcupado) {
        this.idQuarto = idQuarto;
        this.numeroQuarto = numeroQuarto;
        this.capacidade = capacidade;
        this.estaOcupado = estaOcupado;

        // Adiciona o quarto ao array ao ser criado
        if (contadorQuartos < quartos.length) {
            quartos[contadorQuartos] = this;
            contadorQuartos++;
        } else {
            System.out.println("Capacidade máxima de quartos atingida.");
        }

    }

    // Método de criação de quartos iniciais
    public static void criarQuartosIniciais() {
        Quarto quartoSingle1 = new Quarto(1, 101, 1, false); // Quarto 101, capacidade 1
        Quarto quartoSingle2 = new Quarto(2, 102, 1, false); // Quarto 102, capacidade 1
        Quarto quartoCasal1 = new Quarto(3, 103, 2, false); // Quarto 103, capacidade 2
        Quarto quartoCasal2 = new Quarto(4, 104, 2, false); // Quarto 104, capacidade 2
        Quarto quartoCasal3 = new Quarto(5, 105, 2, false); // Quarto 105, capacidade 2
        Quarto quartoCasal4 = new Quarto(6, 201, 2, false); // Quarto 201, capacidade 2
        Quarto quartoCasal5 = new Quarto(7, 202, 2, false); // Quarto 202, capacidade 2
        Quarto quartoFamilia1 = new Quarto(8, 203, 4, false); // Quarto 203, capacidade 4
        Quarto quartoFamilia2 = new Quarto(9, 204, 4, false); // Quarto 204, capacidade 4
        Quarto quartoExclusivo = new Quarto(10, 205, 6, false); // Quarto 205, capacidade 6
    }

    // Métodos de listagem dos quartos
    public static void listarTodosOsQuartos() {
        // Verifica se há quartos cadastrados
        if (contadorQuartos == 0) {
            System.out.println("Nenhum quarto cadastrado.");
            return;
        }
        // Lista todos os quartos cadastrados atraves de um loop de repetição for
        else {
            System.out.println("Lista de todos os quartos:");
            for (int i = 0; i < contadorQuartos; i++) {
                // cria uma variável temporária para armazenar o quarto atual
                Quarto quarto = quartos[i];
                System.out.println("ID: " + quarto.idQuarto + ", Número: " + quarto.numeroQuarto + ", Capacidade: "
                        + quarto.capacidade + ", Ocupado: " + (quarto.estaOcupado ? "Sim" : "Não"));
            }
        }
    }

    // Método para listar quartos disponíveis
    public static void listarQuartosDisponiveis() {
        System.out.println("Lista de quartos disponíveis: ");
        for (int i = 0; i < contadorQuartos; i++) {
            Quarto quarto = quartos[i];
            if (!quarto.estaOcupado) {
                System.out.println("ID: " + quarto.idQuarto + ", Número: " + quarto.numeroQuarto + ", Capacidade: "
                        + quarto.capacidade);
            }
        }
    }

    // Método para listar quartos ocupados
    public static void listarQuartosOcupados() {
        System.out.println("Lista de quartos ocupados: ");
        int quartosOcupados = 0;

        for (int i = 0; i < contadorQuartos; i++) {
            Quarto quarto = quartos[i];
            if (quarto == null) {
                continue;
            }

            if (quarto.estaOcupado) {
                System.out.println("ID: " + quarto.idQuarto +
                        ", Número: " + quarto.numeroQuarto +
                        ", Capacidade: " + quarto.capacidade);

                // Dados de ocupação (hóspede + check-in/out) com base na reserva vigente
                Reservas r = Reservas.getReservaAtivaVigentePorQuarto(quarto.idQuarto);
                System.out.println("  " + Reservas.formatarDadosOcupacao(r));

                quartosOcupados++;
            }
        }

        if (quartosOcupados == 0) {
            System.out.println("Nenhum quarto está ocupado no momento.");
        }
    }

    // Método para listar um quarto específico
    // Falta adicionar todas as reservas do quarto
    public static void listarQuartoEspecifico(int idQuarto) {
        System.out.println(" BUSCA POR QUARTO (ID: " + idQuarto + ")");

        for (int i = 0; i < contadorQuartos; i++) {
            Quarto quarto = quartos[i];
            if (quarto.idQuarto == idQuarto) {
                System.out.println("Quarto encontrado:");
                System.out.println("ID: " + quarto.idQuarto +
                        ", Número: " + quarto.numeroQuarto +
                        ", Capacidade: " + quarto.capacidade +
                        ", Status: " + (quarto.estaOcupado ? "Ocupado" : "Disponível"));
                return;
            }
        }

        System.out.println("Quarto com ID " + idQuarto + " não foi encontrado.");
    }

    // para o codigo que fiz nas reservas funcionar; serve para conseguir aceder ao
    // array de quartos e ao contador de quartos

    public static int getContadorQuartos() {
        return contadorQuartos;
    }

    public static Quarto getQuartoPorIndex(int index) {
        if (index < 0 || index >= contadorQuartos)
            return null;
        return quartos[index];
    }

    public static Quarto getQuartoPorId(int idQuarto) {
        for (int i = 0; i < contadorQuartos; i++) {
            Quarto q = quartos[i];
            if (q != null && q.idQuarto == idQuarto)
                return q;
        }
        return null;
    }
}
