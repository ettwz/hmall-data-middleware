package com.hand.oracle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Vivianus on 2016/12/23.
 */
@Service("staffRepository")
public interface StaffRepository extends JpaRepository<Staff, Integer> {
//    @Transactional
//    @Modifying
//    @Query("UPDATE Staff kts SET kts.AGE=?2 ,kts.DEPT=?3 ,kts.NAME=?1 ,kts.TYPE=?4 WHERE kts.IDD=?5")
//    Staff updateStaff(String name, Integer age,  String dept, String type, Integer idd);

    @Transactional
    @Modifying
    @Query("delete from Staff kts where kts.idd=?1 ")
    int deleteStaff(Integer idd);

    @Transactional
    @Modifying
    @Query("update Staff kts set kts.age=?2 ,kts.dept=?3 ,kts.name=?1 ,kts.type=?4 WHERE kts.idd=?5")
    int updateStaff(String name, Integer age, String dept, String type, Integer idd);
}
