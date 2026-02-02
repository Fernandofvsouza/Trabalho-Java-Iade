import java.util.Scanner;
import model.Hospede;
import model.Quarto;
import model.Reservas;

public class Main {
    public static void main(String[] args) {
        Quarto.criarQuartosIniciais();

        // Carrega dados persistidos (se existirem)
        Hospede.carregarCsv();
        Reservas.carregarCsv();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n===== MENU PRINCIPAL =====");
                System.out.println("1 - Quartos");
                System.out.println("2 - Hóspedes");
                System.out.println("3 - Reservas");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opção: ");

                int opcaoPrincipal;
                try {
                    opcaoPrincipal = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida. Digite um número.");
                    continue;
                }

                switch (opcaoPrincipal) {
                    case 1:
                        menuQuartos(sc);
                        break;
                    case 2:
                        menuHospedes(sc);
                        break;
                    case 3:
                        menuReservas(sc);
                        break;
                    case 0:
                        System.out.println("Encerrando...");
                        return;
                    default:
                        System.out.println("Opção inválida.");
                        break;
                }
            }
        }
    }

    private static void menuQuartos(Scanner sc) {
        while (true) {
            System.out.println("\n===== MENU - QUARTOS =====");
            System.out.println("1 - Listar todos os quartos");
            System.out.println("2 - Listar quartos disponíveis");
            System.out.println("3 - Listar quartos ocupados");
            System.out.println("4 - Buscar quarto por ID");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            int opcao;
            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1:
                    Quarto.listarTodosOsQuartos();
                    break;
                case 2:
                    Quarto.listarQuartosDisponiveis();
                    break;
                case 3:
                    Quarto.listarQuartosOcupados();
                    break;
                case 4:
                    System.out.print("Informe o ID do quarto: ");
                    int id;
                    try {
                        id = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Digite um número.");
                        break;
                    }
                    Quarto.listarQuartoEspecifico(id);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private static void menuHospedes(Scanner sc) {
        while (true) {
            System.out.println("\n===== MENU - HÓSPEDES =====");
            System.out.println("1 - Criar hóspede");
            System.out.println("2 - Listar hóspedes");
            System.out.println("3 - Buscar hóspede por documento");
            System.out.println("4 - Buscar hóspede por ID");
            System.out.println("5 - Editar hóspede");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            int opcao;
            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1: {
                    System.out.print("Nome do hóspede: ");
                    String nome = sc.nextLine();

                    System.out.print("Documento do hóspede: ");
                    String documento = sc.nextLine();

                    Hospede.criarHospede(nome, documento);
                    break;
                }
                case 2:
                    Hospede.listarHospedes();
                    break;
                case 3: {
                    System.out.print("Informe o documento: ");
                    String documento = sc.nextLine();
                    Hospede h = Hospede.procurarHospedePorDocumento(documento);
                    if (h == null) {
                        System.out.println("Hóspede não encontrado para o documento informado.");
                    } else {
                        System.out.println("Hóspede encontrado: ID: " + h.idHospede + " | Nome: " + h.nome + " | Documento: "
                                + h.documento);
                    }
                    break;
                }
                case 4: {
                    System.out.print("Informe o ID do hóspede: ");
                    Integer id = lerInt(sc);
                    if (id == null) {
                        break;
                    }
                    Hospede h = Hospede.getHospedePorId(id);
                    if (h == null) {
                        System.out.println("Hóspede com ID " + id + " não encontrado.");
                    } else {
                        System.out.println("Hóspede encontrado: ID: " + h.idHospede + " | Nome: " + h.nome + " | Documento: "
                                + h.documento);
                    }
                    break;
                }
                case 5: {
                    System.out.print("Informe o ID do hóspede a editar: ");
                    Integer id = lerInt(sc);
                    if (id == null) {
                        break;
                    }

                    System.out.print("Novo nome: ");
                    String novoNome = sc.nextLine();
                    if (novoNome.isBlank()) {
                        novoNome = null;
                    }

                    System.out.print("Novo documento: ");
                    String novoDocumento = sc.nextLine();
                    if (novoDocumento.isBlank()) {
                        novoDocumento = null;
                    }

                    Hospede.editarHospede(id, novoNome, novoDocumento);
                    break;
                }
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private static void menuReservas(Scanner sc) {
        while (true) {
            System.out.println("\n===== MENU - RESERVAS =====");
            System.out.println("1 - Criar reserva (auto-quarto)");
            System.out.println("2 - Listar todas as reservas");
            System.out.println("3 - Listar reservas por quarto (presentes/futuras)");
            System.out.println("4 - Listar reservas por hóspede (presentes/futuras)");
            System.out.println("5 - Editar reserva");
            System.out.println("6 - Cancelar reserva");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            int opcao;
            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1: {
                    System.out.print("Informe o ID do hóspede: ");
                    Integer idHospede = lerInt(sc);
                    if (idHospede == null) {
                        break;
                    }

                    System.out.print("Informe o número de hóspedes: ");
                    Integer numHospedes = lerInt(sc);
                    if (numHospedes == null) {
                        break;
                    }

                    System.out.print("Informe a data de início (YYYY-MM-DD): ");
                    String dataInicio = sc.nextLine().trim();

                    System.out.print("Informe a data de fim (YYYY-MM-DD): ");
                    String dataFim = sc.nextLine().trim();

                    Reservas.criarReservaAutoQuarto(idHospede, numHospedes, dataInicio, dataFim);
                    break;
                }
                case 2:
                    Reservas.listarTodasAsReservas();
                    break;
                case 3: {
                    System.out.print("Informe o ID do quarto: ");
                    Integer idQuarto = lerInt(sc);
                    if (idQuarto == null) {
                        break;
                    }
                    Reservas.listarReservasPorQuarto(idQuarto);
                    break;
                }
                case 4: {
                    System.out.print("Informe o ID do hóspede: ");
                    Integer idHospede = lerInt(sc);
                    if (idHospede == null) {
                        break;
                    }
                    Reservas.listarReservasPorHospede(idHospede);
                    break;
                }
                case 5: {
                    System.out.print("Informe o ID da reserva: ");
                    Integer idReserva = lerInt(sc);
                    if (idReserva == null) {
                        break;
                    }

                    System.out.print("Novo nº de hóspedes (enter para manter): ");
                    String numStr = sc.nextLine().trim();
                    Integer novoNumHospedes = null;
                    if (!numStr.isBlank()) {
                        try {
                            novoNumHospedes = Integer.parseInt(numStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Número inválido.");
                            break;
                        }
                    }

                    System.out.print("Nova data de início (YYYY-MM-DD) (enter para manter): ");
                    String novaDataInicio = sc.nextLine();

                    System.out.print("Nova data de fim (YYYY-MM-DD) (enter para manter): ");
                    String novaDataFim = sc.nextLine();

                    Reservas.editarReserva(idReserva, novoNumHospedes, novaDataInicio, novaDataFim);
                    break;
                }
                case 6: {
                    System.out.print("Informe o ID da reserva: ");
                    Integer idReserva = lerInt(sc);
                    if (idReserva == null) {
                        break;
                    }
                    Reservas.cancelarReserva(idReserva);
                    break;
                }
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private static Integer lerInt(Scanner sc) {
        String s = sc.nextLine().trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido. Digite um número.");
            return null;
        }
    }
}
