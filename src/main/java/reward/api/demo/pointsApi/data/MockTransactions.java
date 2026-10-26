package reward.api.demo.pointsApi.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reward.api.demo.pointsApi.model.Transaction;

import java.io.InputStream;
import java.util.List;

public class MockTransactions {

    private static List<Transaction> transactions;

    public static synchronized List<Transaction> getTransactions() {
        if (transactions == null) {
            transactions = loadTransactions();
        }
        return transactions;
    }

    public static List<Transaction> loadTransactions() {
        try (InputStream is = MockTransactions.class.getResourceAsStream("/mock-transactions.json")) {
            if (is == null) {
                throw new IllegalStateException("mock-transactions.json could not be found in classpath (src/main/resources/)");
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.findAndRegisterModules();
            return mapper.readValue(is, new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            System.err.println("Error reading mock-transactions.json: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read transactions from mock-transactions.json: " + e.getMessage(), e);
        }
    }
}
