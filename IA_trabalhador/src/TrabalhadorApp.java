import java.util.Scanner;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import org.json.JSONObject;

public class TrabalhadorApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nome: ");
        String nome = sc.nextLine();

        System.out.print("Salário Bruto: ");
        double salarioBruto = sc.nextDouble();

        System.out.print("Desconto do INSS: ");
        double descontoInss = sc.nextDouble();

        System.out.print("Número de Dependentes: ");
        int numeroDependentes = sc.nextInt();

        System.out.print("Valor Total de Descontos para Dedução do IRRF: ");
        double descontosIrrf = sc.nextDouble();

        System.out.print("CPF: ");
        String cpf = sc.next();

        if (!validaCpf(cpf)) {
            System.out.println("CPF inválido!");
            sc.close();
            return;
        }

        System.out.print("CEP: ");
        String cep = sc.next();

        String endereco = consultaCep(cep);
        if (endereco == null) {
            System.out.println("CEP inválido!");
            sc.close();
            return;
        }

        double salarioLiquido = calculaSalarioLiquido(salarioBruto, descontoInss, numeroDependentes, descontosIrrf);

        System.out.println("Nome: " + nome);
        System.out.println("Salário Líquido: " + salarioLiquido);
        System.out.println("Endereço: " + endereco);

        try {
            armazenaDados(nome, salarioBruto, descontoInss, numeroDependentes, descontosIrrf, cpf, cep, endereco, salarioLiquido);
        } catch (IOException e) {
            System.out.println("Erro ao armazenar os dados: " + e.getMessage());
        }

        sc.close();
    }

    public static boolean validaCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        return cpf.matches("\\d{11}");
    }

    public static String consultaCep(String cep) {
        try {
            URL url = URI.create("https://viacep.com.br/ws/" + cep + "/json/").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String json = response.toString();

            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("erro")) {
                return null;
            }

            String logradouro = jsonObject.optString("logradouro", "N/A");
            String bairro = jsonObject.optString("bairro", "N/A");
            String localidade = jsonObject.optString("localidade", "N/A");
            String uf = jsonObject.optString("uf", "N/A");

            return logradouro + ", " + bairro + ", " + localidade + " - " + uf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double calculaSalarioLiquido(double salarioBruto, double descontoInss, int numeroDependentes, double descontosIrrf) {
        double salarioBase = salarioBruto - descontoInss - (numeroDependentes * 189.59) - descontosIrrf;
        double irrf = 0;

        if (salarioBase <= 1903.98) {
            irrf = 0;
        } else if (salarioBase <= 2826.65) {
            irrf = salarioBase * 0.075 - 142.80;
        } else if (salarioBase <= 3751.05) {
            irrf = salarioBase * 0.15 - 354.80;
        } else if (salarioBase <= 4664.68) {
            irrf = salarioBase * 0.225 - 636.13;
        } else {
            irrf = salarioBase * 0.275 - 869.36;
        }

        return salarioBruto - descontoInss - irrf;
    }

    public static void armazenaDados(String nome, double salarioBruto, double descontoInss, int numeroDependentes, double descontosIrrf, String cpf, String cep, String endereco, double salarioLiquido) throws IOException {
        File file = new File("trabalhadores.txt");

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        boolean found = false;

        while ((line = reader.readLine()) != null) {
            if (line.contains(cpf)) {
                content.append(nome).append(",").append(salarioBruto).append(",").append(descontoInss).append(",").append(numeroDependentes).append(",").append(descontosIrrf).append(",").append(cpf).append(",").append(cep).append(",").append(endereco).append(",").append(salarioLiquido).append("\n");
                found = true;
            } else {
                content.append(line).append("\n");
            }
        }
        reader.close();

        if (!found) {
            content.append(nome).append(",").append(salarioBruto).append(",").append(descontoInss).append(",").append(numeroDependentes).append(",").append(descontosIrrf).append(",").append(cpf).append(",").append(cep).append(",").append(endereco).append(",").append(salarioLiquido).append("\n");
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content.toString());
        writer.close();
    }
}
