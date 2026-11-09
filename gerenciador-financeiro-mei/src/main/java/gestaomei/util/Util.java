package gestaomei.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Funções de apoio para a interface: converter textos digitados
 * (com vírgula, datas dd/MM/aaaa) e formatar valores em reais.
 */
public class Util {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    /** Aceita "12,50" ou "12.50". Lança NumberFormatException se for inválido. */
    public static double lerDouble(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    public static int lerInt(String texto) {
        return Integer.parseInt(texto.trim());
    }

    /** Lê uma data no formato dd/MM/aaaa. Lança IllegalArgumentException se for inválida. */
    public static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida: use o formato dd/mm/aaaa.");
        }
    }

    public static String formatarData(LocalDate data) {
        return data.format(FORMATO_DATA);
    }

    public static String formatarMoeda(double valor) {
        return MOEDA.format(valor);
    }
}
