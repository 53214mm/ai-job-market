-- =============================================
-- AI 智能招聘市场 - 种子数据
-- Database: ai_job_market
-- 密码统一: 123456 (BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi)
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 用户表 (3用户: 求职者+招聘方+管理员) ====================
INSERT INTO `user` (`id`, `email`, `phone`, `password_hash`, `role`, `nickname`, `avatar_url`, `status`, `created_at`, `updated_at`) VALUES
(1001, 'seeker@test.com', '13800001001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'SEEKER', '张三求职', NULL, 'ACTIVE', NOW(), NOW()),
(1002, 'recruiter@test.com', '13800001002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'RECRUITER', '李HR', NULL, 'ACTIVE', NOW(), NOW()),
(1003, 'admin@test.com', '13800001003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', '管理员', NULL, 'ACTIVE', NOW(), NOW());

INSERT INTO `user_seeker_profile` (`id`, `user_id`, `real_name`, `gender`, `education_level`, `current_city`, `expected_city`, `expected_salary_min`, `expected_salary_max`, `job_status`, `personal_summary`, `created_at`, `updated_at`) VALUES
(2001, 1001, '张三', '男', '本科', '北京', '上海', 25, 45, '在职', '5年Java开发经验，擅长Spring Boot和微服务架构，有大型项目管理和团队协作经验', NOW(), NOW());

INSERT INTO `user_recruiter_profile` (`id`, `user_id`, `real_name`, `position`, `company_id`, `phone`, `verified`, `created_at`, `updated_at`) VALUES
(3001, 1002, '李明', 'HR经理', 4001, '13800002001', TRUE, NOW(), NOW());

-- ==================== 公司 (6家) ====================
INSERT INTO `company` (`id`, `name`, `short_name`, `industry`, `scale`, `stage`, `website`, `address`, `description`, `culture`, `welfare`, `verified`, `status`, `created_at`, `updated_at`) VALUES
(4001, '字节跳动科技有限公司', '字节跳动', '互联网', '1000+', '上市', 'https://www.bytedance.com', '北京市海淀区中关村大街1号', '字节跳动是全球领先的移动互联网科技公司，旗下拥有抖音、今日头条、飞书等知名产品。公司致力于用科技连接世界，为全球用户提供优质的数字化服务。', '追求极致、坦诚清晰、始终创业、多元兼容', '["六险一金","免费三餐","弹性工作","带薪年假15天","住房补贴","股票期权"]', TRUE, 'ACTIVE', NOW(), NOW()),
(4002, '阿里巴巴集团', '阿里巴巴', '互联网', '1000+', '上市', 'https://www.alibaba.com', '浙江省杭州市余杭区文一西路969号', '阿里巴巴集团的使命是让天下没有难做的生意。集团业务包括电商、云计算、数字媒体与娱乐、创新业务等。', '客户第一、团队合作、拥抱变化、诚信正直、激情敬业', '["五险一金","年终奖金","股票期权","带薪年假","免费体检","员工旅游"]', TRUE, 'ACTIVE', NOW(), NOW()),
(4003, '百度在线网络技术有限公司', '百度', '人工智能', '1000+', '上市', 'https://www.baidu.com', '北京市海淀区上地十街10号', '百度是全球最大的中文搜索引擎，也是领先的AI公司。公司拥有自动驾驶、智能语音、知识图谱等核心技术。', '简单可依赖', '["五险一金","弹性工作","免费午餐","健身房","年度体检"]', TRUE, 'ACTIVE', NOW(), NOW()),
(4004, '美团', '美团', '互联网', '1000+', '上市', 'https://www.meituan.com', '北京市朝阳区望京东路6号', '美团以"零售+科技"战略，致力于用科技连接消费者和商家，提供涵盖餐饮、外卖、酒店、旅游等全方位生活服务。', '以客户为中心、长期有耐心', '["五险一金","补充医疗","弹性工作","带薪年假","年度旅游"]', TRUE, 'ACTIVE', NOW(), NOW()),
(4005, '小红书科技', '小红书', '互联网', '500-999', 'D轮', 'https://www.xiaohongshu.com', '上海市黄浦区马当路388号', '小红书是中国领先的生活方式分享平台，月活用户超过2亿。公司业务涵盖社区、电商、广告等。', '真诚分享、友好互动、拥抱多元', '["五险一金","弹性工作","免费零食","健身补贴","宠物友好"]', FALSE, 'ACTIVE', NOW(), NOW()),
(4006, '极客未来科技', '极客未来', '人工智能', '100-499', 'B轮', 'https://www.geekfuture.cn', '深圳市南山区科技园路1号', '极客未来是一家专注于AI+招聘的创业公司，致力于用人工智能技术重塑人才匹配方式。核心产品已服务超过1000家企业客户。', '创新驱动、以人为本、持续学习', '["五险一金","期权激励","弹性工作","远程办公","学习基金"]', FALSE, 'ACTIVE', NOW(), NOW());

-- ==================== 公司评价 (5条) ====================
INSERT INTO `company_review` (`id`, `company_id`, `user_id`, `rating`, `title`, `content`, `pros`, `cons`, `status`, `created_at`) VALUES
(5001, 4001, 1001, 5, '技术氛围浓厚', '在字节跳动实习了3个月，技术氛围非常好，同事都很优秀，学到了很多东西。福利待遇也很给力。', '技术氛围好、福利完善、成长空间大', '工作强度较大、加班较多', 'APPROVED', NOW()),
(5002, 4001, 1003, 4, '不错的平台', '作为互联网大厂，字节给了很好的平台和视野。食堂很好，健身房也很方便。', '平台好、福利好', '节奏快、压力不小', 'APPROVED', NOW()),
(5003, 4002, 1001, 4, '阿里文化深厚', '在阿里工作2年，文化真的很强，每个人都很有激情。技术挑战大，成长快。', '成长快、文化好、技术强', '加班多、内卷严重', 'APPROVED', NOW()),
(5004, 4003, 1001, 3, 'AI基因强但节奏慢', '百度的AI技术确实领先，但公司整体节奏偏慢，适合喜欢钻研的人。', '技术领先、工作稳定', '节奏偏慢、创新不足', 'PENDING', NOW()),
(5005, 4004, 1001, 4, '务实高效', '美团的工作风格很务实，注重结果。业务稳定，福利不错。', '务实、稳定、福利好', '组织架构调整频繁', 'APPROVED', NOW());

-- ==================== 简历 (求职者张三的2份简历) ====================
INSERT INTO `resume` (`id`, `user_id`, `title`, `full_name`, `email`, `phone`, `current_city`, `expected_city`, `expected_salary_min`, `expected_salary_max`, `job_status`, `summary`, `privacy`, `is_default`, `ai_score`, `ai_suggestion`, `created_at`, `updated_at`) VALUES
(6001, 1001, 'Java高级开发工程师简历', '张三', 'zhangsan@email.com', '13800001001', '北京', '上海', 25, 45, '在职', '5年Java后端开发经验，熟练掌握Spring Boot、Spring Cloud微服务架构。主导过日均千万级请求的电商平台后端重构，推动了系统性能提升300%。善于团队协作和技术分享，有带3人小组的经验。对AI和大数据技术有浓厚兴趣，持续学习中。', 'PUBLIC', TRUE, 88, '建议在项目经历中增加量化数据指标，自我评价可以更突出与目标岗位的匹配度', NOW(), NOW()),
(6002, 1001, '校招-前端开发简历', '张三', 'zhangsan@email.com', '13800001001', '北京', '北京', 15, 25, '在职', '计算机专业本科，在校期间参与多个Web项目开发。熟悉Vue.js和React框架，有TypeScript开发经验。热爱前端技术，关注用户体验。', 'PRIVATE', FALSE, 65, '建议补充项目细节和技术难点，增加技能标签', NOW(), NOW());

-- 教育经历
INSERT INTO `resume_education` (`id`, `resume_id`, `school_name`, `degree`, `major`, `start_date`, `end_date`, `description`, `sort_order`, `created_at`) VALUES
(7001, 6001, '清华大学', '本科', '计算机科学与技术', '2015-09-01', '2019-07-01', 'GPA 3.8/4.0，校级优秀毕业生，获得国家奖学金', 0, NOW()),
(7002, 6002, '清华大学', '本科', '计算机科学与技术', '2015-09-01', '2019-07-01', 'GPA 3.8/4.0，ACM竞赛省级银奖', 0, NOW());

-- 工作经历
INSERT INTO `resume_work_experience` (`id`, `resume_id`, `company_name`, `position`, `industry`, `start_date`, `end_date`, `description`, `achievement`, `skills_used`, `sort_order`, `created_at`) VALUES
(8001, 6001, '京东集团', '高级Java开发工程师', '互联网/电商', '2021-07-01', NULL, '负责京东商城订单系统的核心模块开发与维护。主导了订单查询服务从单体到微服务的拆分，接口响应时间从2s降低到200ms。设计并实现了基于Redis的分布式缓存方案，缓存命中率提升到95%。参与双11大促压测和性能优化。', '主导订单系统微服务化改造，性能提升300%', 'Java,Spring Boot,Spring Cloud,Redis,MySQL,Kafka,Docker,Kubernetes', 0, NOW()),
(8002, 6001, '网易', 'Java开发工程师', '互联网/游戏', '2019-07-01', '2021-06-30', '参与网易云音乐后台服务开发，负责用户模块的接口设计和开发。使用Spring Boot搭建RESTful API，对接推荐算法服务。', '新人培训优秀学员，季度之星', 'Java,Spring Boot,MySQL,Redis,Elasticsearch', 1, NOW());

-- 项目经历
INSERT INTO `resume_project` (`id`, `resume_id`, `project_name`, `role`, `start_date`, `end_date`, `description`, `technologies`, `achievement`, `sort_order`, `created_at`) VALUES
(9001, 6001, '电商订单微服务化改造', '技术负责人', '2022-03-01', '2022-09-01', '带领3人团队将单体订单系统拆分为5个微服务：订单查询、订单创建、订单状态、订单支付、订单通知。使用Spring Cloud Gateway做统一网关，Nacos做服务注册与配置中心。实现了灰度发布和滚动升级，确保线上零中断。', 'Java,Spring Cloud,Nacos,Sentinel,Kafka,Redis,Docker,Kubernetes', '性能提升300%，系统可用性达到99.99%', 0, NOW()),
(9002, 6001, '实时数据看板', '核心开发', '2023-01-01', '2023-04-01', '为公司运营团队开发实时数据看板，展示订单量、GMV、转化率等核心指标。使用WebSocket实现实时推送，前端使用ECharts可视化。', 'Java,Spring Boot,WebSocket,Redis,MySQL,ECharts,Vue.js', '支撑双11期间10000+ QPS实时查询', 1, NOW()),
(9003, 6002, '校园二手交易平台', '前端开发', '2022-01-01', '2022-06-01', '独立开发校园二手交易小程序前端，使用uni-app框架实现跨端开发。使用Vue.js组件化开发，封装了20+可复用组件。', 'Vue.js,uni-app,微信小程序,Node.js', '累计用户5000+，日活300+', 0, NOW());

-- 技能
INSERT INTO `resume_skill` (`id`, `resume_id`, `skill_name`, `proficiency`, `months_of_use`, `sort_order`, `created_at`) VALUES
(10001, 6001, 'Java', '精通', 60, 0, NOW()),
(10002, 6001, 'Spring Boot', '精通', 48, 1, NOW()),
(10003, 6001, 'Spring Cloud', '熟练', 36, 2, NOW()),
(10004, 6001, 'MySQL', '精通', 60, 3, NOW()),
(10005, 6001, 'Redis', '熟练', 48, 4, NOW()),
(10006, 6001, 'Kafka', '熟练', 24, 5, NOW()),
(10007, 6001, 'Docker', '熟练', 36, 6, NOW()),
(10008, 6001, 'Kubernetes', '初学', 12, 7, NOW()),
(10009, 6002, 'Vue.js', '熟练', 24, 0, NOW()),
(10010, 6002, 'React', '初学', 12, 1, NOW()),
(10011, 6002, 'TypeScript', '熟练', 18, 2, NOW()),
(10012, 6002, 'Node.js', '初学', 12, 3, NOW());

-- 证书
INSERT INTO `resume_certificate` (`id`, `resume_id`, `cert_name`, `issuing_org`, `obtain_date`, `sort_order`, `created_at`) VALUES
(11001, 6001, 'AWS Certified Solutions Architect', 'Amazon', '2022-06-15', 0, NOW()),
(11002, 6001, 'CET-6 英语六级', '教育部', '2018-12-20', 1, NOW());

-- ==================== 职位 (8个) ====================
INSERT INTO `job` (`id`, `company_id`, `recruiter_id`, `title`, `category`, `experience_level`, `education_level`, `salary_min`, `salary_max`, `salary_months`, `city`, `district`, `address`, `job_type`, `head_count`, `description`, `requirement`, `welfare`, `tags`, `skills_required`, `status`, `view_count`, `apply_count`, `published_at`, `created_at`, `updated_at`) VALUES
(12001, 4001, 1002, '高级Java开发工程师', '技术', '3-5年', '本科', 30, 50, 15, '北京', '海淀区', '中关村大街1号', '全职', 2, '负责字节跳动核心业务系统的架构设计和开发\n参与微服务架构的演进和优化\n指导初中级工程师，进行Code Review\n参与系统性能调优和稳定性建设\n\n我们提供：\n- 抖音级别的技术挑战\n- 行业顶尖的薪资待遇\n- 完善的新人培养体系', '1. 3年以上Java开发经验，精通Spring Boot/Spring Cloud\n2. 熟悉MySQL、Redis、Kafka等中间件\n3. 有分布式系统设计经验\n4. 有微服务架构落地经验优先\n5. 熟悉Docker/Kubernetes优先', '["六险一金","免费三餐","弹性工作","股票期权","带薪年假15天"]', 'Java,微服务,大厂,急聘,高薪', 'Java,Spring Boot,Spring Cloud,MySQL,Redis,Kafka,Docker', 'PUBLISHED', 1523, 86, '2026-05-20 09:00:00', '2026-05-20 08:00:00', NOW()),
(12002, 4001, 1002, 'AI算法工程师（NLP方向）', '技术', '3-5年', '硕士', 40, 70, 16, '北京', '海淀区', '中关村大街1号', '全职', 1, '负责大语言模型的训练和优化\nNLP算法在业务场景中的应用落地\n构建文本理解、情感分析等能力\n与产品团队协作将AI能力产品化', '1. 硕士及以上学历，计算机/AI相关专业\n2. 精通Python和PyTorch\n3. 有NLP相关项目经验\n4. 熟悉Transformer/BERT/GPT等模型\n5. 有大规模分布式训练经验优先', '["六险一金","免费三餐","弹性工作","股票期权","顶配MacBook"]', 'AI,NLP,大语言模型,高薪,深度学习', 'Python,PyTorch,NLP,Transformer,深度学习', 'PUBLISHED', 892, 42, '2026-05-18 10:00:00', '2026-05-18 08:00:00', NOW()),
(12003, 4002, 1002, '高级前端开发工程师', '技术', '3-5年', '本科', 28, 50, 16, '杭州', '余杭区', '文一西路969号', '全职', 2, '负责阿里云控制台前端架构设计和开发\n推动前端组件化和工程化建设\n优化前端性能和用户体验\n参与前端技术选型和最佳实践制定', '1. 精通Vue.js或React框架\n2. 熟悉TypeScript和ES6+\n3. 有大型前端项目架构经验\n4. 了解Node.js和工程化工具\n5. 有数据可视化经验优先', '["五险一金","年终奖金","股票期权","免费体检","员工旅游"]', '前端,Vue.js,大厂,杭州,React', 'Vue.js,React,TypeScript,Node.js,CSS3,Webpack', 'PUBLISHED', 678, 35, '2026-05-19 09:00:00', '2026-05-19 08:00:00', NOW()),
(12004, 4003, 1002, '自动驾驶感知算法工程师', '技术', '3-5年', '硕士', 45, 75, 15, '北京', '海淀区', '上地十街10号', '全职', 1, '负责自动驾驶感知算法的研发和优化\n处理激光雷达、摄像头等多传感器数据\n设计实时目标检测和跟踪算法\n参与实车路测和算法验证', '1. 硕士及以上学历，计算机视觉相关方向\n2. 精通Python/C++，熟悉深度学习框架\n3. 有目标检测/语义分割等经验\n4. 有自动驾驶相关经验优先\n5. 熟悉ROS/OpenCV等工具', '["五险一金","弹性工作","免费午餐","健身房","年度体检"]', 'AI,自动驾驶,计算机视觉,高薪,深度学习', 'Python,C++,PyTorch,OpenCV,ROS,深度学习', 'PUBLISHED', 445, 18, '2026-05-17 11:00:00', '2026-05-17 08:00:00', NOW()),
(12005, 4004, 1002, '产品经理（商家端）', '产品', '1-3年', '本科', 22, 38, 14, '北京', '朝阳区', '望京东路6号', '全职', 1, '负责美团商家端产品的规划和迭代\n收集和分析商家需求，输出产品方案\n与设计、研发团队紧密协作推动落地\n数据驱动产品优化，提升商家满意度', '1. 1年以上B端产品经验\n2. 逻辑清晰，善于数据分析\n3. 有电商或O2O行业经验优先\n4. 熟练使用Axure/Sketch等工具', '["五险一金","补充医疗","弹性工作","年度旅游"]', '产品经理,B端,电商,数据分析', '产品设计,数据分析,SQL,Axure,项目管理', 'PUBLISHED', 321, 12, '2026-05-21 08:00:00', '2026-05-21 07:00:00', NOW()),
(12006, 4005, 1002, 'iOS开发工程师', '技术', '1-3年', '本科', 20, 35, 14, '上海', '黄浦区', '马当路388号', '全职', 2, '负责小红书iOS客户端的开发和维护\n参与App性能优化和用户体验提升\n探索iOS新技术并应用落地\n与产品和设计团队紧密协作', '1. 熟悉Swift/Objective-C开发\n2. 有iOS App上架经验\n3. 了解iOS内存管理和多线程\n4. 有音视频处理经验优先\n5. 有跨平台开发经验加分', '["五险一金","弹性工作","免费零食","健身补贴","宠物友好"]', 'iOS,Swift,上海,年轻化,宠物友好', 'Swift,Objective-C,iOS,UIKit,SwiftUI', 'PUBLISHED', 234, 8, '2026-05-22 09:30:00', '2026-05-22 08:00:00', NOW()),
(12007, 4006, 1002, 'AI产品实习生', '技术', '应届', '本科', 150, 300, 12, '深圳', '南山区', '科技园路1号', '实习', 2, '参与AI招聘匹配系统的产品设计\n协助进行用户调研和竞品分析\n编写产品需求文档和原型设计\n跟进功能开发和测试上线', '1. 计算机或设计相关专业在读\n2. 对AI产品有浓厚兴趣\n3. 有产品实习经验优先\n4. 每周至少实习4天，实习期3个月以上', '["期权激励","弹性工作","远程办公","学习基金","转正机会"]', '实习,AI,产品,深圳,可转正', '产品设计,AI,数据分析,文档撰写', 'PUBLISHED', 567, 45, '2026-05-15 10:00:00', '2026-05-15 08:00:00', NOW()),
(12008, 4006, 1002, '全栈开发工程师', '技术', '1-3年', '本科', 18, 30, 13, '深圳', '南山区', '科技园路1号', '远程', 1, '参与公司核心产品的全栈开发\n负责前端(Vue.js)和后端(Java/Node.js)的接口开发和联调\n参与技术方案设计和数据库建模\n支持线上问题排查和修复', '1. 熟悉Vue.js或React前端框架\n2. 熟悉Java或Node.js后端开发\n3. 了解MySQL、Redis等中间件\n4. 有独立项目经验优先\n5. 远程经验加分', '["期权激励","远程办公","弹性工作","学习基金"]', '全栈,远程,创业公司,Vue.js,Java', 'Vue.js,Java,Spring Boot,MySQL,Redis,Node.js', 'PUBLISHED', 189, 7, '2026-05-23 14:00:00', '2026-05-23 13:00:00', NOW());

-- 技能标签
INSERT INTO `job_skill_tag` (`id`, `job_id`, `skill_name`, `is_required`, `sort_order`, `created_at`) VALUES
(13001, 12001, 'Java', TRUE, 0, NOW()),
(13002, 12001, 'Spring Boot', TRUE, 1, NOW()),
(13003, 12001, 'MySQL', TRUE, 2, NOW()),
(13004, 12001, 'Redis', FALSE, 3, NOW()),
(13005, 12001, 'Docker', FALSE, 4, NOW()),
(13006, 12002, 'Python', TRUE, 0, NOW()),
(13007, 12002, 'PyTorch', TRUE, 1, NOW()),
(13008, 12002, 'NLP', TRUE, 2, NOW()),
(13009, 12003, 'Vue.js', TRUE, 0, NOW()),
(13010, 12003, 'React', FALSE, 1, NOW()),
(13011, 12003, 'TypeScript', TRUE, 2, NOW()),
(13012, 12004, 'Python', TRUE, 0, NOW()),
(13013, 12004, 'C++', TRUE, 1, NOW()),
(13014, 12004, '深度学习', TRUE, 2, NOW()),
(13015, 12005, '数据分析', TRUE, 0, NOW()),
(13016, 12005, 'SQL', TRUE, 1, NOW()),
(13017, 12006, 'Swift', TRUE, 0, NOW()),
(13018, 12006, 'iOS', TRUE, 1, NOW()),
(13019, 12007, 'AI', FALSE, 0, NOW()),
(13020, 12007, '产品设计', TRUE, 1, NOW()),
(13021, 12008, 'Vue.js', TRUE, 0, NOW()),
(13022, 12008, 'Java', TRUE, 1, NOW()),
(13023, 12008, 'Spring Boot', TRUE, 2, NOW()),
(13024, 12008, 'MySQL', FALSE, 3, NOW());

-- ==================== 投递记录 ====================
INSERT INTO `application` (`id`, `job_id`, `resume_id`, `seeker_id`, `recruiter_id`, `company_id`, `status`, `cover_letter`, `ai_match_score`, `ai_match_reason`, `created_at`, `updated_at`) VALUES
(14001, 12001, 6001, 1001, 1002, 4001, 'INTERVIEW', '我对字节跳动的技术挑战非常感兴趣，过去5年的Java微服务开发经验让我有信心胜任这个岗位。我主导过的订单系统重构项目与贵公司的技术方向高度契合。', 92, '技能高度匹配(Java/Spring Boot/MySQL/Redis)，项目经验契合，期望薪资在范围内', '2026-05-22 10:00:00', NOW()),
(14002, 12003, 6001, 1001, 1002, 4002, 'APPLIED', '虽然我主要是后端开发，但对前端技术也有浓厚的兴趣和基础。我做过数据看板的前端部分，使用过Vue.js和ECharts。希望有机会转型全栈方向。', 68, '主要技能方向偏后端，但基础技术栈有交集', '2026-05-23 11:00:00', NOW()),
(14003, 12008, 6001, 1001, 1002, 4006, 'SCREENING', '我对全栈开发和远程工作非常感兴趣，5年的后端经验加上前端项目经历让我具备了全栈视野。希望能加入一个有活力的创业团队，一起成长。', 85, '技能匹配度高(Java/Spring Boot/Vue.js/MySQL)，项目经验丰富', '2026-05-24 09:30:00', NOW()),
(14004, 12002, 6001, 1001, 1002, 4001, 'VIEWED', '虽然是Java开发背景，但我对AI技术非常感兴趣，一直在自学Python和机器学习。希望有机会跨入AI领域。', 45, '技能方向与岗位要求差距较大，建议先通过内部转岗或参加AI培训项目', '2026-05-24 14:00:00', NOW()),
(14005, 12007, 6002, 1001, 1002, 4006, 'REJECTED', '我是在校生，想找一份AI产品相关的实习。有前端开发背景，对AI产品设计非常感兴趣。', 72, '基础能力匹配，但职位要求应届生，目前该求职者已有多年工作经验', '2026-05-21 15:00:00', NOW());

-- 投递状态日志
INSERT INTO `application_log` (`id`, `application_id`, `from_status`, `to_status`, `operator_id`, `remark`, `created_at`) VALUES
(15001, 14001, 'APPLIED', 'VIEWED', 1002, '简历已查看', '2026-05-22 11:00:00'),
(15002, 14001, 'VIEWED', 'SCREENING', 1002, '初步筛选通过', '2026-05-22 14:00:00'),
(15003, 14001, 'SCREENING', 'INTERVIEW', 1002, '邀请面试', '2026-05-23 09:00:00'),
(15004, 14003, 'APPLIED', 'VIEWED', 1002, NULL, '2026-05-24 10:00:00'),
(15005, 14003, 'VIEWED', 'SCREENING', 1002, '进入筛选', '2026-05-24 15:00:00'),
(15006, 14004, 'APPLIED', 'VIEWED', 1002, NULL, '2026-05-24 14:30:00'),
(15007, 14005, 'APPLIED', 'VIEWED', 1002, NULL, '2026-05-21 16:00:00'),
(15008, 14005, 'VIEWED', 'REJECTED', 1002, '感谢投递，经验方向不太匹配', '2026-05-22 09:00:00');

-- ==================== 面试安排 ====================
INSERT INTO `interview` (`id`, `application_id`, `interview_type`, `scheduled_time`, `duration_minutes`, `location`, `interviewer`, `contact_phone`, `status`, `feedback`, `created_at`) VALUES
(16001, 14001, 'VIDEO', '2026-05-27 14:00:00', 60, '飞书视频会议', '王技术总监', '13800002001', 'SCHEDULED', NULL, NOW());

-- ==================== 收藏 ====================
INSERT INTO `favorite` (`id`, `user_id`, `target_type`, `target_id`, `created_at`) VALUES
(17001, 1001, 'JOB', 12001, NOW()),
(17002, 1001, 'JOB', 12002, NOW()),
(17003, 1001, 'JOB', 12008, NOW()),
(17004, 1001, 'JOB', 12005, NOW());

-- ==================== 系统通知 ====================
INSERT INTO `notification` (`id`, `user_id`, `type`, `title`, `content`, `related_id`, `is_read`, `created_at`) VALUES
(18001, 1001, 'INTERVIEW_INVITE', '面试邀请', '您好，您在字节跳动投递的高级Java开发工程师职位，我们已安排于2026-05-27 14:00进行视频面试，请准时参加。', 14001, FALSE, '2026-05-23 10:00:00'),
(18002, 1001, 'APPLICATION_UPDATE', '投递状态更新', '您在极客未来科技投递的全栈开发工程师职位状态已更新为「筛选中」', 14003, FALSE, '2026-05-24 15:00:00'),
(18003, 1001, 'APPLICATION_UPDATE', '投递结果通知', '您在极客未来科技投递的AI产品实习生职位未被通过，感谢您的关注。', 14005, TRUE, '2026-05-22 09:30:00'),
(18004, 1002, 'SYSTEM', '新投递提醒', '您发布的高级Java开发工程师职位收到1份新投递', 12001, TRUE, '2026-05-22 10:30:00');

-- ==================== 站内私信 ====================
INSERT INTO `message` (`id`, `sender_id`, `receiver_id`, `application_id`, `content`, `is_read`, `created_at`) VALUES
(19001, 1002, 1001, 14001, '张三您好，您的简历我们已收到，非常匹配我们的岗位！请问您什么时候方便面试？', FALSE, '2026-05-22 14:30:00'),
(19002, 1001, 1002, 14001, '你好李HR，谢谢关注！我这周工作日都可以，下午2点以后比较方便。', TRUE, '2026-05-22 15:00:00');

-- ==================== 求职攻略文章 ====================
INSERT INTO `article` (`id`, `category_id`, `author_id`, `title`, `summary`, `content`, `tags`, `view_count`, `status`, `published_at`, `created_at`, `updated_at`) VALUES
(20001, 1, 1003, '2026年互联网大厂面试指南', '整理了字节、阿里、腾讯等大厂的最新面试流程和常见问题', '本文详细介绍了当前互联网大厂的面试流程、技术面常见问题、行为面试的应答策略以及薪资谈判技巧。包含多道真实面试题和参考答案。', '面试,大厂,求职攻略,经验分享', 1523, 'PUBLISHED', '2026-05-15 08:00:00', '2026-05-15 07:00:00', NOW()),
(20002, 1, 1003, '程序员简历这样写，面试邀约翻3倍', 'HR视角的简历优化指南，从排版到内容全面解析', '简历是求职的第一关。本文从HR的实际视角出发，告诉你如何写出一份让招聘方眼前一亮的简历。涵盖简历结构、关键词优化、量化成就展示等多个维度。', '简历优化,求职,程序员,面试', 2891, 'PUBLISHED', '2026-05-12 09:00:00', '2026-05-12 08:00:00', NOW()),
(20003, 3, 1003, '从字节面经看大厂面试趋势', '一位求职者的真实面试经历和复盘', '分享我在字节跳动三轮技术面试的完整经历，包括算法题、系统设计、项目经历面试的详细复盘，以及我的准备策略和心得体会。', '面经,字节跳动,后端,面试复盘', 892, 'PUBLISHED', '2026-05-18 10:00:00', '2026-05-18 08:00:00', NOW()),
(20004, 4, 1003, 'Java开发者的2026年技术栈升级路线', '从Spring Boot到微服务全栈，最新技术栈推荐', '随着AI时代的到来，Java开发者需要掌握哪些新技能？本文梳理了2026年Java开发者的技术栈升级路线，涵盖AI集成、云原生、响应式编程等方向。', 'Java,技术栈,职业发展,微服务', 678, 'PUBLISHED', '2026-05-20 08:00:00', '2026-05-20 07:00:00', NOW()),
(20005, 2, 1003, 'AI招聘行业2026年展望', '人工智能如何重塑人才市场', '2026年，AI在招聘领域的应用正在从"辅助工具"升级为"核心引擎"。本文分析了AI简历匹配、智能面试、人才画像等最新技术趋势及其对求职者和招聘方的影响。', 'AI招聘,行业趋势,职业发展', 445, 'PUBLISHED', '2026-05-25 08:00:00', '2026-05-25 07:00:00', NOW());

-- ==================== 系统字典 ====================
INSERT INTO `sys_dict` (`id`, `dict_type`, `dict_key`, `dict_value`, `sort_order`, `remark`, `created_at`) VALUES
(21001, 'job_status', 'DRAFT', '草稿', 0, '职位草稿状态', NOW()),
(21002, 'job_status', 'PUBLISHED', '已发布', 1, '职位已发布', NOW()),
(21003, 'job_status', 'CLOSED', '已关闭', 2, '职位已关闭', NOW()),
(21004, 'application_status', 'APPLIED', '已投递', 0, NULL, NOW()),
(21005, 'application_status', 'VIEWED', '已查看', 1, NULL, NOW()),
(21006, 'application_status', 'SCREENING', '筛选中', 2, NULL, NOW()),
(21007, 'application_status', 'INTERVIEW', '面试中', 3, NULL, NOW()),
(21008, 'application_status', 'OFFER', '已发Offer', 4, NULL, NOW()),
(21009, 'application_status', 'HIRED', '已录用', 5, NULL, NOW()),
(21010, 'application_status', 'REJECTED', '已拒绝', 6, NULL, NOW()),
(21011, 'application_status', 'WITHDRAWN', '已撤回', 7, NULL, NOW()),
(21012, 'privacy_level', 'PUBLIC', '公开', 0, '所有人可见', NOW()),
(21013, 'privacy_level', 'APPLICATION_ONLY', '仅投递可见', 1, '投递时展示', NOW()),
(21014, 'privacy_level', 'PRIVATE', '私密', 2, '仅自己可见', NOW()),
(21015, 'industry', '互联网', '互联网', 0, NULL, NOW()),
(21016, 'industry', '人工智能', '人工智能', 1, NULL, NOW()),
(21017, 'industry', '金融', '金融', 2, NULL, NOW()),
(21018, 'industry', '教育', '教育', 3, NULL, NOW()),
(21019, 'industry', '医疗', '医疗', 4, NULL, NOW()),
(21020, 'industry', '电商', '电商', 5, NULL, NOW()),
(21021, 'industry', '游戏', '游戏', 6, NULL, NOW()),
(21022, 'industry', '硬件', '硬件', 7, NULL, NOW());

-- ==================== AI 对话示例 ====================
INSERT INTO `ai_chat_session` (`id`, `user_id`, `session_type`, `title`, `message_count`, `created_at`, `updated_at`) VALUES
(22001, 1001, 'CAREER_ADVICE', '求职咨询', 4, NOW(), NOW());

INSERT INTO `ai_chat_message` (`id`, `session_id`, `role`, `content`, `tokens_used`, `created_at`) VALUES
(23001, 22001, 'USER', '我现在是Java开发工程师，想了解一下2026年的职业发展方向', 20, '2026-05-24 10:00:00'),
(23002, 22001, 'ASSISTANT', '根据当前技术趋势分析，Java开发者在2026年有三个重要的发展方向：\n1. AI/大模型应用开发：学习Spring AI、LangChain4j等框架\n2. 云原生架构：深入Kubernetes、Service Mesh\n3. 全栈发展：结合Vue.js/React + Node.js\n\n你的Java基础扎实，建议优先接触AI集成方向。', 150, '2026-05-24 10:00:05'),
(23003, 22001, 'USER', 'AI集成方向需要学哪些具体技术？', 15, '2026-05-24 10:01:00'),
(23004, 22001, 'ASSISTANT', 'AI集成方向推荐学习路径：\n1. Spring AI（已有基础，可快速上手）\n2. 向量数据库（pgvector/Milvus）\n3. Prompt工程\n4. RAG架构\n5. 对话记忆管理（如你项目中的RedisBasedChatMemory）\n\n建议从Spring AI开始，它和Spring Boot无缝集成。', 180, '2026-05-24 10:01:10');

SET FOREIGN_KEY_CHECKS = 1;
