<template>
    <div class="page-container">
        <div class="hero-banner">
            <div class="hero-content">
                <h1>斌安居酒店</h1>
                <p>舒适入住 · 智慧服务 · 长沙核心商圈</p>
                <div class="hero-stats" v-if="publicStats.availableRooms !== undefined">
                    <div class="hero-stat-item">
                        <span class="num">{{ publicStats.availableRooms }}</span>
                        <span class="txt">可订房间</span>
                    </div>
                    <div class="hero-stat-item">
                        <span class="num">{{ publicStats.todayOccupancyRate }}%</span>
                        <span class="txt">今日入住率</span>
                    </div>
                    <div class="hero-stat-item">
                        <span class="num">{{ publicStats.totalRooms }}</span>
                        <span class="txt">客房总数</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="carousel">
            <el-carousel height="300px" indicator-position="outside">
                <el-carousel-item>
                    <img src="@/assets/introduction.jpg" class="carousel-img" />
                </el-carousel-item>
                <el-carousel-item>
                    <img src="@/assets/jiudian.jpeg" class="carousel-img" />
                </el-carousel-item>
                <el-carousel-item>
                    <img src="@/assets/jiudian2.jpg" class="carousel-img" />
                </el-carousel-item>
            </el-carousel>
        </div>

        <div class="section" v-if="recommendList.length">
            <div class="section-title">
                <i class="el-icon-magic-stick"></i> 智能推荐
            </div>
            <div class="recommend-bar">
                <div
                    v-for="item in recommendList"
                    :key="item.categoryId"
                    class="recommend-item"
                    @click="$router.push(`/front/room?categoryId=${item.categoryId}`)"
                >
                    <img :src="$baseFileUrl + item.photo" class="rec-img" />
                    <div class="rec-info">
                        <div class="rec-name">{{ item.categoryName }}</div>
                        <div class="rec-reason">{{ item.recommendReason }}</div>
                        <div class="rec-meta">
                            <span class="rec-price">¥{{ item.price }}/晚</span>
                            <el-tag size="mini" :type="item.availableRooms > 0 ? 'success' : 'info'">
                                {{ item.availableRooms > 0 ? `余${item.availableRooms}间` : '已满' }}
                            </el-tag>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="section">
            <div class="section-title">
                客房展示
            </div>
            <div class="section-list">
                <div v-for="item in categoryList" :key="item.id" class="section-item" @click="$router.push(`/front/room?categoryId=${item.id}`)">
                    <img :src="$baseFileUrl+item.photo" class="hotel-img">
                    <div class="hotel-title">{{ item.categoryName }}</div>
                    <div class="flex justify-between item-center w-full">
                        <div class="hotel-price">￥{{ item.price }}<span class="unit">/晚</span></div>
                    </div>
                </div>
                <div class="section-item-empty"></div>
                <div class="section-item-empty"></div>
            </div>
        </div>

        <div class="section">
            <div class="section-title">
                酒店简介
            </div>
            <div class="hotel-introduction">
                <div class="introduction">
                    斌安居酒店位于长沙市商业核心区域举步可至繁华的五一广场及多家高级时尚购物中心。
                    如果你想探索充满活力的不夜城长沙或附近城市，你会发现酒店可接驳多条高速公路、快速路便捷通过城市繁忙的交通。
                    酒店拥有三百余间宽敞舒适的客房及套房，采用明亮淡雅的色调和丰富的木料，精心营造出希尔顿现代典雅的风格。高端的现代科技应用于房内的各项设施，
                    客房内可开式全景落地玻璃窗带来自然的光线，饱览白云新城美景。
                    3,220平方米会议及宴会场地，其中包括1个1,500平方米的壮观典雅的无柱式大宴会厅，内设有83平方米高清LED屏幕(P2分辨率)。
                    4间风格迥异的餐厅及酒吧让您饕餮羊城美食。希尔顿特色水疗中心，装潢华丽的私人护理套房，让客人立即进入宁静的冥想世界。
                    装备完善的健身房、室内恒温游泳池，以及蒸汽房、桑拿、按摩池和旋水池等让你释放淋漓激情。无论商务或休闲，斌安居酒店是你下榻长沙的无忧之选。
                </div>
                <div class="hotel-img">
                    <img src="@/assets/introduction.jpg" />
                </div>
            </div>
        </div>

        <div class="section">
            <div class="section-title">
                最新公告
            </div>
            <el-table class="my-20" :data="noticeList" border fit highlight-current-row size="small">
                <el-table-column align="center" label="ID" prop="id" width="60"></el-table-column>
                <el-table-column align="center" label="标题" prop="title" show-overflow-tooltip></el-table-column>
                <el-table-column align="center" label="发布时间" prop="createTime" width="180"></el-table-column>
                <el-table-column fixed="right" label="操作" width="100">
                    <template slot-scope="scope">
                        <el-button @click="$router.push(`/front/noticeDetails/${scope.row.id}`)" type="text" size="small">查看</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <AiChat />
    </div>
</template>

<script>
import { getTop5NoticeAPI } from '@/api/notice'
import { findAllCategoryAPI } from '@/api/category'
import { getPublicStatsAPI } from '@/api/stats'
import AiChat from '@/components/AiChat.vue'

export default {
    name: 'Hotely5dWebHome',
    components: { AiChat },

    data() {
        return {
            noticeList: [],
            categoryList: [],
            publicStats: {},
            recommendList: []
        };
    },

    async mounted() {
        const { data: noticeList } = await getTop5NoticeAPI()
        this.noticeList = noticeList
        const { data: categoryList } = await findAllCategoryAPI()
        this.categoryList = categoryList
        try {
            const { data } = await getPublicStatsAPI()
            this.publicStats = data
            this.recommendList = (data.categories || []).filter(c => c.recommend).slice(0, 3)
        } catch (e) { /* public stats optional */ }
    }
};
</script>

<style lang="less" scoped>
.hero-banner {
    background: linear-gradient(135deg, #141820 0%, #1c2333 50%, #252d3d 100%);
    border-radius: 14px;
    padding: 48px 44px;
    color: #fff;
    margin-bottom: 28px;
    border: 1px solid rgba(201, 169, 98, 0.12);
    box-shadow: 0 8px 32px rgba(20, 24, 32, 0.1);
    position: relative;
    overflow: hidden;

    &::after {
        content: "";
        position: absolute;
        right: -60px;
        top: -60px;
        width: 240px;
        height: 240px;
        background: radial-gradient(circle, rgba(201, 169, 98, 0.15) 0%, transparent 70%);
        pointer-events: none;
    }

    h1 { font-size: 34px; margin: 0 0 10px; font-weight: 700; letter-spacing: 2px; }
    p { margin: 0 0 24px; color: #c9a962; font-size: 15px; letter-spacing: 1px; }
}

.hero-stats {
    display: flex;
    gap: 40px;

    .hero-stat-item {
        .num { display: block; font-size: 28px; font-weight: 700; }
        .txt { font-size: 13px; opacity: 0.8; }
    }
}

.carousel {
    margin-bottom: 24px;
    border-radius: 8px;
    overflow: hidden;

    .carousel-img {
        width: 100%;
        height: 300px;
        object-fit: cover;
    }
}

.recommend-bar {
    display: flex;
    gap: 16px;
    margin: 16px 0;
    flex-wrap: wrap;
}

.recommend-item {
    flex: 1;
    min-width: 280px;
    display: flex;
    gap: 12px;
    padding: 14px;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        transform: translateY(-2px);
    }

    .rec-img { width: 80px; height: 60px; object-fit: cover; border-radius: 6px; }
    .rec-name { font-weight: 600; font-size: 15px; color: #303133; }
    .rec-reason { font-size: 12px; color: #909399; margin: 4px 0; }
    .rec-meta { display: flex; align-items: center; gap: 8px; }
    .rec-price { color: #f56c6c; font-weight: 600; font-size: 14px; }
}

.page-container {
    .section-list {
            margin: 20px 0;
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;

            &::after {
                content: '';
                width: 290px;
            }

            .section-item-empty {
                width: 290px;
            }

            .section-item {
                width: 290px;
                height: 300px;
                padding: 16px;
                box-sizing: border-box;
                margin-bottom: 10px;
                cursor: pointer;

                &:nth-child(4n-3) {
                    margin-left: 0 !important;
                }

                .hotel-img {
                    width: 100%;
                    height: 200px;
                    object-fit: cover;
                    border-radius: 6px;
                }

                .hotel-title {
                    font-weight: bold;
                    width: 100%;
                    margin: 10px 0 4px;
                    text-overflow: ellipsis;
                    overflow: hidden;
                    white-space: nowrap;
                }

                .hotel-price {
                    width: 100%;
                    margin: 4px;
                    text-align: right;
                    font-size: 18px;

                    .unit { font-size: 12px; font-weight: normal; color: #909399; }
                }
            }
        }

    .hotel-introduction {
        display: flex;
        justify-content: space-around;
        align-items: center;
        background: #fff;
        border-radius: 12px;
        padding: 28px;
        box-shadow: 0 2px 12px rgba(20, 24, 32, 0.06);

        .hotel-img {
            flex: 1;
            margin: 20px 10px;

            img {
                width: 100%;
                height: 350px;
                border-radius: 8px;
                object-fit: cover;
            }
        }

        .introduction {
            height: 350px;
            flex: 1;
            line-height: 1.8;
            color: #606266;
            font-size: 15px;
        }
    }
}
</style>
