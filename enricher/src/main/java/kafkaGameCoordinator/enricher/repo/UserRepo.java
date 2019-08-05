package kafkaGameCoordinator.enricher.repo;

import kafkaGameCoordinator.enricher.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
