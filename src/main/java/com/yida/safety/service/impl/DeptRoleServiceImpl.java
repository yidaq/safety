package com.yida.safety.service.impl;

import com.yida.safety.common.BaseResponseCode;
import com.yida.safety.dao.SysDeptRoleMapper;
import com.yida.safety.exception.BusinessException;
import com.yida.safety.pojo.SysDeptRole;
import com.yida.safety.pojo.SysUserRole;
import com.yida.safety.service.DeptRoleService;
import com.yida.safety.vo.req.DeptOwnRoleReqVO;
import com.yida.safety.vo.req.UserOwnRoleReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: safety
 * @description:
 * @author: YiDa
 * @create: 2020-04-29 11:35
 **/
@Service
public class DeptRoleServiceImpl implements DeptRoleService {

    @Autowired
    private SysDeptRoleMapper sysDeptRoleMapper;


    @Override
    public List<String> getRolsIdsByDeptId(String deptId) {
        return sysDeptRoleMapper.getRoleIdsByDeptId(deptId);
    }

    @Override
    public void addDeptRoleInfo(DeptOwnRoleReqVO vo) {
        //删除他们关联数据
        sysDeptRoleMapper.removeRoleByDeptId(vo.getDeptId());
        if(vo.getRoleIds()==null||vo.getRoleIds().isEmpty()){
            return;
        }
        List<SysDeptRole> list=new ArrayList<>();
        for (String roleId: vo.getRoleIds()) {
            SysDeptRole sysDeptRole=new SysDeptRole();
            sysDeptRole.setId(UUID.randomUUID().toString());
            sysDeptRole.setCreateTime(new Date());
            sysDeptRole.setDeptId(vo.getDeptId());
            sysDeptRole.setRoleId(roleId);
            list.add(sysDeptRole);
        }
        int i = sysDeptRoleMapper.batchInsertDeptRole(list);
        if(i==0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<String> getDeptIdsByRoleId(String roleId) {
        return sysDeptRoleMapper.selectDeptIdByRoleId(roleId);
    }

    @Override
    public void removeKeyByRoleId(String roleId) {
        int i = sysDeptRoleMapper.removeKeyByRoleId(roleId);
        if (i!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }
}
