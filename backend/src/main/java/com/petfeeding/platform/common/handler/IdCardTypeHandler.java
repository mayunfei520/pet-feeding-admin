package com.petfeeding.platform.common.handler;

import com.petfeeding.platform.common.util.Sm4Util;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * feeders.id_card 透明加解密：仅通过 Feeder.idCard 上的 @TableField(typeHandler=...) 局部生效，
 * 不作为全局 TypeHandler 注册（否则所有 String 字段都会被加解密，导致查询参数被加密而查不到数据）。
 */
public class IdCardTypeHandler extends BaseTypeHandler<String> {

    private final Sm4Util sm4Util;

    public IdCardTypeHandler() {
        this.sm4Util = Sm4Util.getInstance();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, sm4Util.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return sm4Util.decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return sm4Util.decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return sm4Util.decrypt(cs.getString(columnIndex));
    }
}
