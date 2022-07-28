package cn.crazykid.qqrobot.service.impl

import cn.crazykid.qqrobot.dao.intf.GroupDao
import cn.crazykid.qqrobot.service.IGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @author CrazyKid
 * @date 2022/7/27 09:24
 */
@Service
class GroupServiceImpl : IGroupService {
    @Autowired
    private lateinit var groupDao: GroupDao
}
