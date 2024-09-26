package com.evolutionnext.cqrs;

import org.postgresql.ds.PGConnectionPoolDataSource;

import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateStocks {
    public static void main(String[] args) throws InterruptedException {
        PGConnectionPoolDataSource source = new PGConnectionPoolDataSource();
        source.setURL("jdbc:postgresql://localhost:5432/");
        source.setUser("docker");
        source.setPassword("docker");
        source.setDatabaseName("docker");

        List<String> stockSymbols = List.of("ADBE", "ADI", "ADP", "ADSK", "AEP", "ALGN", "AMAT", "AMGN", "ANSS", "ASML", "ATVI", "AVGO", "AZN", "BIIB", "BKNG", "BKR", "CDNS", "CEG", "CHTR", "CMCSA", "COST", "CPRT", "CRWD", "CSGP", "CSX", "CTAS", "CTSH", "DDOG", "DLTR", "DXCM", "EA", "EBAY", "ENPH", "EXC", "FAST", "FISV", "FTNT", "GFS", "GILD", "GOOG", "GOOGL", "HON", "IDXX", "ILMN", "INTU", "ISRG", "KDP", "KHC", "KLAC", "LCID", "LRCX", "LULU", "MAR", "MCHP", "MDLZ", "MNST", "MRNA", "MRVL", "NFLX", "NXPI", "ODFL", "ORLY", "PANW", "PAYX", "PCAR", "PDD", "PYPL", "QCOM",
            "REGN", "ROST", "SBUX", "SGEN", "SIRI", "SNPS", "TEAM", "TMUS", "TXN", "VRSK", "VRTX", "WBA", "WBD", "WDAY", "XEL", "ZM", "ZS");

        AtomicBoolean done = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> done.set(true)));

        Random random = new Random();
        while (!done.get()) {
            Long generatedKey = null;
            try (Connection connection = source.getConnection();
                 PreparedStatement preparedStatement =
                     connection.prepareStatement
                         ("INSERT INTO stock_trade (stock_symbol, trade_timestamp, trade_type, amount) values (?, ?, ?, ?);",
                             Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, stockSymbols.get(random.nextInt(stockSymbols.size() - 1)));
                preparedStatement.setTimestamp(2, new Timestamp(Instant.now().toEpochMilli()));
                preparedStatement.setString(3, random.nextBoolean() ? "buy" : "sell");
                preparedStatement.setFloat(4, random.nextFloat(0, 1000));
                preparedStatement.execute();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                generatedKeys.next();
                generatedKey = generatedKeys.getLong(1);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
            System.out.println(generatedKey);
            Thread.sleep(random.nextInt(10000 - 5000) + 5000);
        }
    }
}
