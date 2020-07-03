package cn.waynechu.utility.domain.convert;

import cn.waynechu.springcloud.common.util.CollectionUtil;
import cn.waynechu.utility.dal.dataobject.DictionaryDO;
import cn.waynechu.utility.facade.response.DictionaryResponse;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuwei
 * @since 2020/7/3 18:50
 */
@UtilityClass
public class DictionaryConvert {

    public static List<DictionaryResponse> toDictionaryResponseList(List<DictionaryDO> dictionaryDOList) {
        List<DictionaryResponse> returnValue = new ArrayList<>();

        if (CollectionUtil.isNotNullOrEmpty(dictionaryDOList)) {
            DictionaryResponse dictionaryResponse;
            for (DictionaryDO dictionaryDO : dictionaryDOList) {
                dictionaryResponse = new DictionaryResponse();
                BeanUtils.copyProperties(dictionaryDO, dictionaryResponse);
                returnValue.add(dictionaryResponse);
            }
        }
        return returnValue;
    }
}
