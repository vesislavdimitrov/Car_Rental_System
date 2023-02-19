package db;

import cars.Car;
import cars.Trip;
import logging.LoggerManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class TripDAO extends EntryDAO<Trip> {

    private static final Logger LOGGER = LoggerManager.getLogger(TripDAO.class.getName());

    public TripDAO() {
        super("rental_history", "rent_id");
    }
    @Override
    protected String buildCreationQuery(Object object) {
        return "INSERT INTO "
                + this.tableName +
                "(client_email, car_make, car_model, rent_time,return_time) VALUES(?, ?, ?, ?, ?)";
    }

    @Override
    protected void setValues(PreparedStatement statement, Object object) throws SQLException {
        if (object instanceof Trip trip) {
            statement.setString(1, trip.getClientEmail());
            statement.setString(2, trip.getMake());
            statement.setString(3, trip.getModel());
            statement.setTimestamp(4, Timestamp.valueOf((trip.getRentTime())));
            if (trip.getReturnTime().isPresent()) {
                statement.setTimestamp(5, Timestamp.valueOf(trip.getReturnTime().get()));
            } else {
                statement.setNull(5, Types.TIMESTAMP);
            }
        }
    }

    @Override
    protected Trip mapReadResultSetToObject(ResultSet resultSet) {
        try {
            LocalDateTime returnTime = null;
            Timestamp returnTimestamp = resultSet.getTimestamp("return_time");
            if (returnTimestamp != null) {
                returnTime = returnTimestamp.toLocalDateTime();
            }
            return new Trip(
                    resultSet.getString("rent_id"),
                    resultSet.getString("client_email"),
                    resultSet.getString("car_make"),
                    resultSet.getString("car_model"),
                    resultSet.getTimestamp("rent_time").toLocalDateTime(),
                    Optional.ofNullable(returnTime)
            );
        } catch (SQLException e) {
            LOGGER.warning(e+" Couldn't construct object!");
        }
        return null;
    }
    @Override
    protected String getKey(Trip object) {
        return object.getId();
    }

    @Override
    protected void setUpdatedValues(PreparedStatement statement, int propertyIndex, Object updatedValue) {
        try {
            switch (propertyIndex){
                case 1,2,3-> statement.setString(1,(String) updatedValue);
                case 4,5 -> statement.setTimestamp(1, Timestamp.valueOf((LocalDateTime) updatedValue));
            }
        } catch (SQLException | NumberFormatException e) {
            LOGGER.warning(e + " Error updating car!");
        }
    }
    @Override
    protected String buildUpdateQuery(int propertyIndex) {
        Map<Integer, String> columnMap = Map.of(
                1, "client_email",
                2, "car_make",
                3, "car_model",
                4, "rent_time",
                5, "return_time"
        );
        String columnName = columnMap.get(propertyIndex);
        return "UPDATE " + this.tableName + " SET " + columnName + "=? WHERE " + this.tablePrimaryKey + "=?";
    }

    public Trip getTripByClient(String email){
        for(Trip t: read().values()){
            if(t.getClientEmail().equals(email)){
                return t;
            }
        }
        return null;
    }

    public Car getTripCar(String email) {
        for (Trip t : read().values()) {
            if (t.getClientEmail().equals(email) && t.getReturnTime().isEmpty()){
                return new CarDAO().getCarByModel(t.getModel());
            }
        }
        return null;
    }
}