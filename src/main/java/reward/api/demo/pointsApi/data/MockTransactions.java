package reward.api.demo.pointsApi.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reward.api.demo.pointsApi.model.Transaction;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MockTransactions {

    public static final List<Transaction> TRANSACTIONS = loadTransactions();

    public static List<Transaction> getTransactions() {
        return TRANSACTIONS;
    }

    public static List<Transaction> loadTransactions() {
        try (InputStream is = MockTransactions.class.getResourceAsStream("/mock-transactions.json")) {
            if (is == null) {
                throw new IllegalStateException("mock-transactions.json could not be found in src/main/resources/");
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.readValue(is, new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to read transactions from mock-transactions.json", e);
        }
    }
}
