package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hospede {
    public int idHospede;
    public String nome;
    public String documento;

    private static final int MAX_HOSPEDES = 1000;
    private static final Hospede[] hospedes = new Hospede[MAX_HOSPEDES];
    private static int contadorHospedes = 0;
    private static int proximoIdHospede = 1;

    private static final Path CSV_PATH = Paths.get("data", "hospedes.csv");

    private Hospede(String nome, String documento) {
        this.idHospede = proximoIdHospede++;
        this.nome = nome;
        this.documento = documento;
    }

    // Construtor interno para carga (mantém ID do arquivo)
    private Hospede(int idHospede, String nome, String documento) {
        this.idHospede = idHospede;
        this.nome = nome;
        this.documento = documento;
    }

    /**
     * Cria hóspede com ID auto-incremental.
     */
    public static boolean criarHospede(String nome, String documento) {
        if (contadorHospedes >= hospedes.length) {
            System.out.println("Não foi possível criar hóspede: limite atingido.");
            return false;
        }
        if (!validarNome(nome) || !validarDocumento(documento)) {
            return false;
        }
        if (procurarHospedePorDocumento(documento) != null) {
            System.out.println("Não foi possível criar hóspede: documento já cadastrado.");
            return false;
        }

        Hospede h = new Hospede(nome.trim(), documento.trim());
        hospedes[contadorHospedes++] = h;
        System.out.println("Hóspede criado com sucesso! ID: " + h.idHospede);

        salvarCsv();
        return true;
    }

    public static void listarHospedes() {
        if (contadorHospedes == 0) {
            System.out.println("Nenhum hóspede cadastrado.");
            return;
        }

        System.out.println("Lista de hóspedes:");
        for (int i = 0; i < contadorHospedes; i++) {
            Hospede h = hospedes[i];
            if (h == null) {
                continue;
            }
            System.out.println(formatarHospedeLinha(h));
        }
    }

    public static Hospede procurarHospedePorDocumento(String documento) {
        if (documento == null) {
            return null;
        }
        String doc = documento.trim();
        if (doc.isBlank()) {
            return null;
        }

        for (int i = 0; i < contadorHospedes; i++) {
            Hospede h = hospedes[i];
            if (h == null) {
                continue;
            }
            if (h.documento != null && h.documento.equalsIgnoreCase(doc)) {
                return h;
            }
        }
        return null;
    }

    /**
     * Edita nome e/ou documento (quando não nulos/não vazios).
     */
    public static boolean editarHospede(int idHospede, String novoNome, String novoDocumento) {
        Hospede h = getHospedePorId(idHospede);
        if (h == null) {
            System.out.println("Hóspede com ID " + idHospede + " não encontrado.");
            return false;
        }

        if (novoNome != null && !novoNome.isBlank()) {
            if (!validarNome(novoNome)) {
                return false;
            }
            h.nome = novoNome.trim();
        }

        if (novoDocumento != null && !novoDocumento.isBlank()) {
            if (!validarDocumento(novoDocumento)) {
                return false;
            }
            Hospede existente = procurarHospedePorDocumento(novoDocumento);
            if (existente != null && existente.idHospede != h.idHospede) {
                System.out.println("Não foi possível editar hóspede: documento já cadastrado em outro hóspede.");
                return false;
            }
            h.documento = novoDocumento.trim();
        }

        System.out.println("Hóspede " + idHospede + " editado com sucesso.");
        salvarCsv();
        return true;
    }

    /**
     * Necessário para Reservas.existeHospede (usado via reflexão).
     */
    public static Hospede getHospedePorId(int idHospede) {
        for (int i = 0; i < contadorHospedes; i++) {
            Hospede h = hospedes[i];
            if (h != null && h.idHospede == idHospede) {
                return h;
            }
        }
        return null;
    }

    // =====================
    // Persistência CSV
    // =====================

    /** Carrega hóspedes do CSV (se existir). Deve ser chamado no início do programa. */
    public static void carregarCsv() {
        if (!Files.exists(CSV_PATH)) {
            return;
        }

        // limpa a memória atual
        for (int i = 0; i < contadorHospedes; i++) {
            hospedes[i] = null;
        }
        contadorHospedes = 0;
        proximoIdHospede = 1;

        int maxId = 0;

        try (BufferedReader br = Files.newBufferedReader(CSV_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) {
                    continue;
                }
                if (isFirst) {
                    // suporta cabeçalho "id;nome;documento"
                    isFirst = false;
                    if (line.toLowerCase().startsWith("id;")) {
                        continue;
                    }
                }

                String[] parts = line.split(";", -1);
                if (parts.length < 3) {
                    continue;
                }

                Integer id = tryParseInt(parts[0]);
                if (id == null) {
                    continue;
                }

                String nome = parts[1];
                String documento = parts[2];

                if (contadorHospedes >= hospedes.length) {
                    break;
                }

                Hospede h = new Hospede(id, nome, documento);
                hospedes[contadorHospedes++] = h;
                if (id > maxId) {
                    maxId = id;
                }
            }
        } catch (IOException e) {
            System.out.println("Falha ao ler hóspedes do CSV: " + e.getMessage());
        }

        proximoIdHospede = maxId + 1;
    }

    /** Salva hóspedes em CSV (sobrescreve). */
    public static void salvarCsv() {
        try {
            Path parent = CSV_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter bw = Files.newBufferedWriter(CSV_PATH, StandardCharsets.UTF_8)) {
                bw.write("id;nome;documento");
                bw.newLine();
                for (int i = 0; i < contadorHospedes; i++) {
                    Hospede h = hospedes[i];
                    if (h == null) {
                        continue;
                    }
                    // simples: evita quebrar o CSV
                    String nome = safeCsv(h.nome);
                    String doc = safeCsv(h.documento);
                    bw.write(h.idHospede + ";" + nome + ";" + doc);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Falha ao salvar hóspedes no CSV: " + e.getMessage());
        }
    }

    private static String safeCsv(String s) {
        if (s == null) {
            return "";
        }
        // remove separador/linhas para não corromper o CSV
        return s.replace(";", " ").replace("\n", " ").replace("\r", " ").trim();
    }

    private static Integer tryParseInt(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // validações/helpers

    private static boolean validarNome(String nome) {
        if (nome == null || nome.trim().isBlank()) {
            System.out.println("Nome inválido: não pode ser vazio.");
            return false;
        }
        return true;
    }

    private static boolean validarDocumento(String documento) {
        if (documento == null || documento.trim().isBlank()) {
            System.out.println("Documento inválido: não pode ser vazio.");
            return false;
        }
        return true;
    }

    private static String formatarHospedeLinha(Hospede h) {
        return "Hóspede ID: " + h.idHospede +
                " | Nome: " + h.nome +
                " | Documento: " + h.documento;
    }
}
