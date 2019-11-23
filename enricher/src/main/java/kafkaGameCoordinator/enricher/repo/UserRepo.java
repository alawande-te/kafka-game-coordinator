package kafkaGameCoordinator.enricher.repo;

import kafkaGameCoordinator.enricher.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class UserRepo {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<User> rowMapper = BeanPropertyRowMapper.newInstance(User.class);
    private static final ResultSetExtractor<Map<Long, User>> resultSetExtractor = new UserIdExtractor();

    @Autowired
    public UserRepo(NamedParameterJdbcTemplate jdbtcTemplate) {
        this.jdbcTemplate = jdbtcTemplate;
    }

    public Map<Long, User> getUsersByIdIn(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = "SELECT * " +
                     "FROM user " +
                     "LEFT JOIN user_status USING(user_id) " +
                     "WHERE user_id IN (:userIds) ";

        MapSqlParameterSource params = new MapSqlParameterSource("userIds", userIds);

        return jdbcTemplate.query(sql, params, resultSetExtractor);
    }

    public void saveAll(Collection<User> users) {

        SqlParameterSource[] params = users.stream()
                                            .map(user -> new MapSqlParameterSource()
                                                    .addValue("userId", user.getUserId())
                                                    .addValue("rank", user.getRank())
                                                    .addValue("updatedTs", user.getUpdatedTs())
                                                    .addValue("status", user.getStatus().toString()))
                                            .toArray(SqlParameterSource[]::new);

        String sql = "UPDATE user u " +
                     "JOIN user_status us ON u.user_id = us.user_id " +
                     "SET " +
                     "u.rank = :rank, " +
                     "us.status = :status, " +
                     "us.updated_ts = :updatedTs " +
                     "WHERE u.user_id = :userId ";

        jdbcTemplate.batchUpdate(sql, params);
    }

    private static class UserIdExtractor implements ResultSetExtractor<Map<Long, User>> {

        @Override
        public Map<Long, User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, User> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getLong("user_id"),
                           rowMapper.mapRow(rs, rs.getRow()));
            }
            return result;
        }
    }
}
