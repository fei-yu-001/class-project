package com.salary.config;

import com.salary.entity.Employee;
import com.salary.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeDataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    private static final List<String> HEROES = List.of(
            // 天罡星三十六
            "呼保义 宋江", "玉麒麟 卢俊义", "智多星 吴用", "入云龙 公孙胜", "大刀 关胜", "豹子头 林冲",
            "霹雳火 秦明", "双鞭 呼延灼", "小李广 花荣", "小旋风 柴进", "扑天雕 李应", "美髯公 朱仝",
            "花和尚 鲁智深", "行者 武松", "双枪将 董平", "没羽箭 张清", "青面兽 杨志", "金枪手 徐宁",
            "急先锋 索超", "神行太保 戴宗", "赤发鬼 刘唐", "黑旋风 李逵", "九纹龙 史进", "没遮拦 穆弘",
            "插翅虎 雷横", "混江龙 李俊", "立地太岁 阮小二", "船火儿 张横", "短命二郎 阮小五", "浪里白条 张顺",
            "活阎罗 阮小七", "病关索 杨雄", "拼命三郎 石秀", "两头蛇 解珍", "双尾蝎 解宝", "浪子 燕青",
            // 地煞星七十二
            "神机军师 朱武", "镇三山 黄信", "病尉迟 孙立", "丑郡马 宣赞", "井木犴 郝思文", "百胜将 韩滔",
            "天目将 彭玘", "圣水将 单廷圭", "神火将 魏定国", "圣手书生 萧让", "铁面孔目 裴宣", "摩云金翅 欧鹏",
            "火眼狻猊 邓飞", "锦毛虎 燕顺", "锦豹子 杨林", "轰天雷 凌振", "神算子 蒋敬", "小温侯 吕方",
            "赛仁贵 郭盛", "神医 安道全", "紫髯伯 皇甫端", "矮脚虎 王英", "一丈青 扈三娘", "丧门神 鲍旭",
            "混世魔王 樊瑞", "毛头星 孔明", "独火星 孔亮", "八臂哪吒 项充", "飞天大圣 李衮", "玉臂匠 金大坚",
            "铁笛仙 马麟", "出洞蛟 童威", "翻江蜃 童猛", "玉幡竿 孟康", "通臂猿 侯健", "跳涧虎 陈达",
            "白花蛇 杨春", "白面郎君 郑天寿", "九尾龟 陶宗旺", "铁扇子 宋清", "铁叫子 乐和", "花项虎 龚旺",
            "中箭虎 丁得孙", "小遮拦 穆春", "操刀鬼 曹正", "云里金刚 宋万", "摸着天 杜迁", "病大虫 薛永",
            "金眼彪 施恩", "打虎将 李忠", "小霸王 周通", "金钱豹子 汤隆", "鬼脸儿 杜兴", "出林龙 邹渊",
            "独角龙 邹润", "旱地忽律 朱贵", "笑面虎 朱富", "铁臂膊 蔡福", "一枝花 蔡庆", "催命判官 李立",
            "青眼虎 李云", "没面目 焦挺", "石将军 石勇", "小尉迟 孙新", "母大虫 顾大嫂", "菜园子 张青",
            "母夜叉 孙二娘", "活闪婆 王定六", "险道神 郁保四", "白日鼠 白胜", "鼓上蚤 时迁", "金毛犬 段景住"
    );

    private static final List<PositionDept> POSITIONS = List.of(
            new PositionDept("P001", "D001"),
            new PositionDept("P002", "D001"),
            new PositionDept("P003", "D001"),
            new PositionDept("P004", "D002"),
            new PositionDept("P005", "D002"),
            new PositionDept("P006", "D003"),
            new PositionDept("P007", "D003"),
            new PositionDept("P008", "D004"),
            new PositionDept("P009", "D004")
    );

    @Override
    public void run(String... args) {
        long count = employeeRepository.count();
        if (count >= HEROES.size()) {
            log.info("当前员工数量 {}，跳过假数据初始化", count);
            return;
        }

        for (int i = 0; i < HEROES.size(); i++) {
            PositionDept pd = POSITIONS.get(i % POSITIONS.size());
            String fullName = HEROES.get(i);
            String name = fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ") + 1) : fullName;
            String gender = (fullName.contains("扈三娘") || fullName.contains("顾大嫂") || fullName.contains("孙二娘")) ? "女" : "男";
            Employee emp = Employee.builder()
                    .empName(name)
                    .gender(gender)
                    .birthDate(randomDate(1985, 1998))
                    .hireDate(randomDate(2018, 2024))
                    .empStatus("在职")
                    .posId(pd.posId)
                    .deptCode(pd.deptCode)
                    .build();
            employeeRepository.save(emp);
        }
        log.info("已初始化 {} 条水浒好汉员工数据", HEROES.size());
    }

    private LocalDate randomDate(int startYear, int endYear) {
        int year = ThreadLocalRandom.current().nextInt(startYear, endYear + 1);
        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 28);
        return LocalDate.of(year, month, day);
    }

    private record PositionDept(String posId, String deptCode) {
    }
}
