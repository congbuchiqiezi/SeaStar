package com.rongseal.db.com.rongseal.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;



// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig friendDaoConfig;
    private final DaoConfig userDaoConfig;
    private final DaoConfig groupDaoConfig;

    private final FriendDao friendDao;
    private final UserDao userDao;
    private final GroupDao groupDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        friendDaoConfig = daoConfigMap.get(FriendDao.class).clone();
        friendDaoConfig.initIdentityScope(type);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        groupDaoConfig = daoConfigMap.get(GroupDao.class).clone();
        groupDaoConfig.initIdentityScope(type);

        friendDao = new FriendDao(friendDaoConfig, this);
        userDao = new UserDao(userDaoConfig, this);
        groupDao = new GroupDao(groupDaoConfig, this);

        registerDao(Friend.class, friendDao);
        registerDao(User.class, userDao);
        registerDao(Group.class, groupDao);
    }
    
    public void clear() {
        friendDaoConfig.getIdentityScope().clear();
        userDaoConfig.getIdentityScope().clear();
        groupDaoConfig.getIdentityScope().clear();
    }

    public FriendDao getFriendDao() {
        return friendDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

}
