package com.yida.safety.service.impl;

import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.dao.SysUserDeptMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysUserDept;
import com.yida.safety.service.DeptUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-29 11:45
 **/
@Service
public class DeptUserServiceImpl implements DeptUserService {

    @Autowired
    private SysUserDeptMapper sysUserDeptMapper;

    @Override
    public String getDeptIdsByUserId(String userId) {
       List<String> list = sysUserDeptMapper.getDeptIdsByUserId(userId);
       if(list.size() != 0){
           return list.get(0);
       }
       return null;
    }

    @Override
    public List<String> getUserIdByDeptId(String deptId) {
        return sysUserDeptMapper.getUserIdByDeptId(deptId);
    }

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return sysUserDeptMapper.getRoleIdsByUserId(userId);
    }

    @Override
    public List<SysUserDept> selectAll() {
        return sysUserDeptMapper.selectAll();
    }

    @Override
    public void deleteKeyByUserId(String userId) {
        String oldId = sysUserDeptMapper.selectIdByUserId(userId);
        if(oldId!=null&&!oldId.isEmpty()){
            int i =  sysUserDeptMapper.deleteKeyByUserId(userId);
            if (i != 1){
                throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
            }
        }
    }
}
