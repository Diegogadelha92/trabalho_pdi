import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public abstract class Processador {

    public static BufferedImage paraPB(BufferedImage image) {
        int altura = image.getHeight();
        int largura = image.getWidth();

        for (int lin = 0; lin < largura; lin++){
            for (int col = 0; col < altura; col++) {

                int pixel = image.getRGB(lin, col);
                Color cor = new Color(pixel);

                int pixelCorCinza = (int) ((cor.getRed() * .3) + (cor.getGreen() * .6) + (cor.getBlue() * .1));

                Color novaCor = new Color(pixelCorCinza);
                Color cinza = new Color(novaCor.getBlue(),novaCor.getBlue(),novaCor.getBlue());

                image.setRGB(lin, col, cinza.getRGB());

            }
        }
        return image;
    }

    public static BufferedImage binarizacao(BufferedImage image, int limiar) {

        BufferedImage imagemBin = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        int altura = imagemBin.getHeight();
        int largura = imagemBin.getWidth();

        for (int lin = 0; lin < largura; lin++){
            for (int col = 0; col < altura; col++){

                int pixel = image.getRGB(lin, col);
                Color cor = new Color(pixel);

                if (cor.getRed() >= limiar){
                    imagemBin.setRGB(lin, col, Color.white.getRGB());
                } else {
                    imagemBin.setRGB(lin, col, Color.black.getRGB());
                }

            }

        }

        return imagemBin;

    }

    public static BufferedImage saltAndPepper(BufferedImage image, double noiseLevel) {
        BufferedImage novaImagem = copiarImagem(image);
        Random random = new Random();

        int largura = novaImagem.getWidth();
        int altura = novaImagem.getHeight();

        for (int lin = 0; lin < largura; lin++) {
            for (int col = 0; col < altura; col++) {
                double randomValue = random.nextDouble();

                if (randomValue < noiseLevel) {
                    if (random.nextBoolean()) {
                        novaImagem.setRGB(lin, col, Color.WHITE.getRGB());
                    } else {
                        novaImagem.setRGB(lin, col, Color.BLACK.getRGB());
                    }
                }
            }
        }

        return novaImagem;
    }

    public static BufferedImage redimensionar(BufferedImage original, int novaLargura, int novaAltura) {
        BufferedImage nova = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_ARGB);

        int larguraOriginal = original.getWidth();
        int alturaOriginal = original.getHeight();

        for (int linha = 0; linha < novaLargura; linha++) {
            for (int coluna = 0; coluna < novaAltura; coluna++) {
                int srcLinha = linha * larguraOriginal / novaLargura;
                int srcColuna = coluna * alturaOriginal / novaAltura;
                int pixel = original.getRGB(srcLinha, srcColuna);
                nova.setRGB(linha, coluna, pixel);
            }
        }

        return nova;
    }

    public static BufferedImage redimensionarPorProporcao(BufferedImage original, int fatorEscala) {
        int novaLargura = Math.max(1, original.getWidth() * fatorEscala / 100);
        int novaAltura = Math.max(1, original.getHeight() * fatorEscala / 100);
        return redimensionar(original, novaLargura, novaAltura);
    }

    public static BufferedImage rotacionar(BufferedImage imagem, double angulo) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        double radianos = Math.toRadians(angulo);
        double seno = Math.abs(Math.sin(radianos));
        double cosseno = Math.abs(Math.cos(radianos));

        int novaLargura = (int) Math.floor((largura * cosseno) + (altura * seno));
        int novaAltura = (int) Math.floor((altura * cosseno) + (largura * seno));

        BufferedImage imagemRotacionada = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagemRotacionada.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, novaLargura, novaAltura);
        g2d.translate((novaLargura - largura) / 2.0, (novaAltura - altura) / 2.0);
        g2d.rotate(radianos, largura / 2.0, altura / 2.0);
        g2d.drawRenderedImage(imagem, null);
        g2d.dispose();

        return imagemRotacionada;
    }


    public static BufferedImage sobel(BufferedImage image) {
        int[][] gx = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        int[][] gy = {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };
        return aplicarFiltroDuplo(image, gx, gy);
    }

    public static BufferedImage prewittDuplo(BufferedImage image) {
        int[][] gx = {
                {-1, 0, 1},
                {-1, 0, 1},
                {-1, 0, 1}
        };
        int[][] gy = {
                {-1, -1, -1},
                {0, 0, 0},
                {1, 1, 1}
        };
        return aplicarFiltroDuplo(image, gx, gy);
    }

    public static BufferedImage laplaceGaussiano(BufferedImage image) {
        int[][] kernel = {
                {0, 0, -1, 0, 0},
                {0, -1, -2, -1, 0},
                {-1, -2, 16, -2, -1},
                {0, -1, -2, -1, 0},
                {0, 0, -1, 0, 0}
        };
        return aplicarKernel(image, kernel);
    }

    private static BufferedImage aplicarFiltroDuplo(BufferedImage image, int[][] gx, int[][] gy) {
        BufferedImage cinza = paraCinza(image);
        BufferedImage saida = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 1; x < image.getWidth() - 1; x++) {
            for (int y = 1; y < image.getHeight() - 1; y++) {
                int somaX = 0;
                int somaY = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int tom = new Color(cinza.getRGB(x + i, y + j)).getRed();
                        somaX += tom * gx[i + 1][j + 1];
                        somaY += tom * gy[i + 1][j + 1];
                    }
                }

                int valor = limitar((int) Math.sqrt((somaX * somaX) + (somaY * somaY)));
                saida.setRGB(x, y, new Color(valor, valor, valor).getRGB());
            }
        }

        return saida;
    }

    private static BufferedImage aplicarKernel(BufferedImage image, int[][] kernel) {
        BufferedImage cinza = paraCinza(image);
        BufferedImage saida = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int tamanho = kernel.length;
        int metade = tamanho / 2;

        for (int x = metade; x < image.getWidth() - metade; x++) {
            for (int y = metade; y < image.getHeight() - metade; y++) {
                int soma = 0;

                for (int i = -metade; i <= metade; i++) {
                    for (int j = -metade; j <= metade; j++) {
                        int tom = new Color(cinza.getRGB(x + i, y + j)).getRed();
                        soma += tom * kernel[i + metade][j + metade];
                    }
                }

                int valor = limitar(Math.abs(soma));
                saida.setRGB(x, y, new Color(valor, valor, valor).getRGB());
            }
        }

        return saida;
    }

    private static BufferedImage paraCinza(BufferedImage image) {
        BufferedImage cinza = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color cor = new Color(image.getRGB(x, y));
                int tom = (int) ((cor.getRed() * 0.3) + (cor.getGreen() * 0.6) + (cor.getBlue() * 0.1));
                cinza.setRGB(x, y, new Color(tom, tom, tom).getRGB());
            }
        }

        return cinza;
    }

    private static int limitar(int valor) {
        if (valor < 0) {
            return 0;
        }
        if (valor > 255) {
            return 255;
        }
        return valor;
    }

    private static BufferedImage copiarImagem(BufferedImage image) {
        BufferedImage copia = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copia.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copia;
    }
}
