import spark.Request;
import spark.Response;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static spark.Spark.*;

public class Servidor {

    public static void main(String[] args) {
        port(8080);
        liberarCors();

        post("/processar", (Request request, Response response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String operacao = pegarCampo(request, "operacao", "salt-pepper");
            StringBuilder json = new StringBuilder("[");
            int total = 0;

            for (Part parte : request.raw().getParts()) {
                if (!parte.getName().equals("arquivos") || parte.getSize() == 0) {
                    continue;
                }

                BufferedImage imagem = ImageIO.read(parte.getInputStream());
                if (imagem == null) {
                    continue;
                }

                BufferedImage processada = aplicarFiltro(imagem, operacao, request);
                String base64 = converterParaBase64(processada);

                if (total > 0) {
                    json.append(",");
                }

                json.append("{\"imagem\":\"").append(base64).append("\"}");
                total++;
            }

            json.append("]");

            if (total == 0) {
                halt(400, "Nenhuma imagem foi enviada.");
            }

            response.type("application/json");
            return json.toString();
        });

        System.out.println("Servidor rodando em http://localhost:8080");
    }

    private static BufferedImage aplicarFiltro(BufferedImage imagem, String operacao, Request request) {
        if (operacao.equals("salt-pepper")) {
            double nivelRuido = Double.parseDouble(pegarCampo(request, "nivelRuido", "0.05"));
            return Processador.saltAndPepper(imagem, nivelRuido);
        }

        if (operacao.equals("redimensionar")) {
            int escala = Integer.parseInt(pegarCampo(request, "escala", "50"));
            return Processador.redimensionarPorProporcao(imagem, escala);
        }

        if (operacao.equals("rotacionar")) {
            double angulo = Double.parseDouble(pegarCampo(request, "angulo", "45"));
            return Processador.rotacionar(imagem, angulo);
        }

        if (operacao.equals("sobel")) {
            return Processador.sobel(imagem);
        }

        if (operacao.equals("laplace-gaussiano")) {
            return Processador.laplaceGaussiano(imagem);
        }

        if (operacao.equals("prewitt-duplo")) {
            return Processador.prewittDuplo(imagem);
        }

        return imagem;
    }

    private static String converterParaBase64(BufferedImage imagem) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", saida);
        return Base64.getEncoder().encodeToString(saida.toByteArray());
    }

    private static String pegarCampo(Request request, String nome, String valorPadrao) {
        try {
            Part parte = request.raw().getPart(nome);
            if (parte != null) {
                return new String(parte.getInputStream().readAllBytes()).trim();
            }
        } catch (Exception e) {
            return valorPadrao;
        }
        return valorPadrao;
    }

    private static void liberarCors() {
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        });

        options("/*", (request, response) -> "OK");
    }
}
