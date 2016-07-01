package com.miracle.db.dao;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.miracle.db.bean.GluValue;

public class GluValueDao {
	private Context context;
	private Dao<GluValue, Integer> mDao = null;
	private RuntimeExceptionDao<GluValue, Integer> userRuntimeDao = null;
	private DatabaseHelper helper;
	GluValue gluValue;

	public GluValueDao(Context context) {
		this.context = context;
		try {
			helper = DatabaseHelper.getHelper(context);
			// 初始化DAO
			mDao = getUserDao(helper);
			userRuntimeDao = getUserDataDao(helper);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Dao<GluValue, Integer> getUserDao(DatabaseHelper helper) throws SQLException {
		if (mDao == null)
			mDao = helper.getDao(GluValue.class);
		return mDao;
	}

	private RuntimeExceptionDao<GluValue, Integer> getUserDataDao(DatabaseHelper helper) {
		if (userRuntimeDao == null) {
			userRuntimeDao = helper.getRuntimeExceptionDao(GluValue.class);
		}
		return userRuntimeDao;
	}

	/**
	 * 增加一个用户
	 * 
	 * @param gluValue
	 * @throws SQLException
	 */
	public void add(GluValue gluValue) {
		/*
		 * //事务操作
		 * TransactionManager.callInTransaction(helper.getConnectionSource(),
		 * new Callable<Void>() {
		 * 
		 * @Override public Void call() throws Exception { return null; } });
		 */
		try {
			mDao.create(gluValue);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public GluValue get(int id) {
		try {
			return mDao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 插入值
	 */
	public void insert(GluValue gluValue) {
		userRuntimeDao.createIfNotExists(gluValue);
	}

	/**
	 * 更新
	 * 
	 * @param gluValue
	 *            待更新的user
	 */
	public void update(GluValue gluValue) {
		userRuntimeDao.createOrUpdate(gluValue);
		// mUserDAO.update(user);
	}

	/**
	 * 按照指定的id 与 username 删除一项
	 * 
	 * @param id
	 * @param username
	 * @return 删除成功返回true ，失败返回false
	 */
	public int delete(String username) {
		try {
			// 删除指定的信息，类似delete User where 'id' = id ;
			DeleteBuilder<GluValue, Integer> deleteBuilder = userRuntimeDao.deleteBuilder();
			deleteBuilder.where().eq("username", username);

			return deleteBuilder.delete();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 按照id查询user
	 * 
	 * @param id
	 * @return
	 */
	public GluValue search(String username) {
		try {
			// 查询的query 返回值是一个列表
			// 类似 select * from User where 'username' = username;
			List<GluValue> gluValues = userRuntimeDao.queryBuilder().where().eq("username", username).query();
			if (gluValues.size() > 0)
				return gluValues.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 删除全部
	 */
	public void deleteAll() {
		userRuntimeDao.delete(queryAll());
	}

	/**
	 * 查询所有的
	 */
	public List<GluValue> queryAll() {
		List<GluValue> gluValues = userRuntimeDao.queryForAll();
		return gluValues;
	}

	/**
	 * 显示所有的
	 */
	private void display(TextView view) {
		List<GluValue> gluValues = queryAll();
		for (GluValue gluValue : gluValues) {
			view.append(gluValue.toString());
		}
	}

}
