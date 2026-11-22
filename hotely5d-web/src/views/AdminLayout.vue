<template>
  <div :class="['admin-layout', isCollapse ? 'sidebar-collapsed' : 'sidebar-expanded']">
    <aside class="sidebar">
      <div class="sidebar-brand" @click="$router.push('/admin')">
        <img src="@/assets/logo.png" class="brand-logo" alt="logo" />
        <div v-show="!isCollapse" class="brand-text">
          <span class="brand-name">{{ $sysTitle }}</span>
          <span class="brand-sub">管理控制台</span>
        </div>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <el-menu
          :collapse="isCollapse"
          :default-active="activeMenu"
          :collapse-transition="false"
          background-color="transparent"
          text-color="rgba(255,255,255,0.72)"
          active-text-color="#e2cc8a"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/admin">
            <i class="el-icon-s-home"></i>
            <span slot="title">工作台</span>
          </el-menu-item>

          <el-submenu index="group-data">
            <template slot="title">
              <i class="el-icon-data-analysis"></i>
              <span>数据看板</span>
            </template>
            <el-menu-item index="/admin/statistics">
              <i class="el-icon-s-data"></i>
              <span slot="title">数据统计</span>
            </el-menu-item>
            <el-menu-item index="/admin/intelligence">
              <i class="el-icon-cpu"></i>
              <span slot="title">数智决策</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="group-ops">
            <template slot="title">
              <i class="el-icon-s-cooperation"></i>
              <span>运营管理</span>
            </template>
            <el-menu-item index="/admin/appointment">
              <i class="el-icon-s-flag"></i>
              <span slot="title">预约管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <i class="el-icon-s-order"></i>
              <span slot="title">入住管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/category">
              <i class="el-icon-s-promotion"></i>
              <span slot="title">房型管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/room">
              <i class="el-icon-office-building"></i>
              <span slot="title">房间管理</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="group-sys">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/admin">
              <i class="el-icon-user"></i>
              <span slot="title">管理员</span>
            </el-menu-item>
            <el-menu-item index="/admin/member">
              <i class="el-icon-user-solid"></i>
              <span slot="title">用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/notice">
              <i class="el-icon-bell"></i>
              <span slot="title">公告管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/message">
              <i class="el-icon-chat-dot-round"></i>
              <span slot="title">留言管理</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-scrollbar>

      <div v-show="!isCollapse" class="sidebar-footer">
        <span>斌安居酒店管理系统</span>
      </div>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <button class="collapse-btn" @click="isCollapse = !isCollapse">
            <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-root">管理后台</span>
            <i class="el-icon-arrow-right"></i>
            <span class="breadcrumb-current">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-block">
              <div class="user-avatar">{{ avatarText }}</div>
              <span class="user-name">{{ user.name || '管理员' }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile" icon="el-icon-s-custom">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { removeToken } from '@/utils/auth'

const TITLE_MAP = {
  '/admin': '工作台',
  '/admin/statistics': '数据统计',
  '/admin/intelligence': '数智决策',
  '/admin/admin': '管理员管理',
  '/admin/member': '用户管理',
  '/admin/category': '房型管理',
  '/admin/room': '房间管理',
  '/admin/appointment': '预约管理',
  '/admin/orders': '入住管理',
  '/admin/message': '留言管理',
  '/admin/notice': '公告管理',
  '/admin/adminInfo': '个人信息',
  '/admin/addOrders': '新增入住'
}

export default {
  name: 'AdminLayout',
  computed: {
    ...mapState(['user']),
    activeMenu() {
      return this.$route.path
    },
    pageTitle() {
      return TITLE_MAP[this.$route.path] || '管理后台'
    },
    avatarText() {
      const name = this.user?.name || '管'
      return name.charAt(0)
    }
  },
  data() {
    return {
      isCollapse: false
    }
  },
  methods: {
    ...mapMutations(['setUser', 'setToken']),
    handleCommand(cmd) {
      if (cmd === 'profile') {
        this.$router.push('/admin/adminInfo')
      } else if (cmd === 'logout') {
        this.logout()
      }
    },
    logout() {
      this.$confirm('确认退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeToken()
        this.setUser({})
        this.setToken('')
        this.$router.replace('/front')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="less" scoped>
@sidebar-w: 240px;
@sidebar-collapsed-w: 64px;
@gold: #c9a962;
@gold-light: #e2cc8a;

.admin-layout {
  min-height: 100vh;
  background: var(--hotel-bg, #f0f2f5);
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  z-index: 1001;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #141820 0%, #1a2030 50%, #141820 100%);
  border-right: 1px solid rgba(201, 169, 98, 0.12);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar-expanded .sidebar { width: @sidebar-w; }
.sidebar-collapsed .sidebar { width: @sidebar-collapsed-w; }

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;

  .brand-logo {
    width: 36px;
    height: 36px;
    object-fit: contain;
    flex-shrink: 0;
  }

  .brand-text {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .brand-name {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }

  .brand-sub {
    font-size: 11px;
    color: @gold;
    margin-top: 2px;
    letter-spacing: 1px;
    text-transform: uppercase;
  }
}

.sidebar-scroll {
  flex: 1;
  overflow: hidden;
}

.sidebar-footer {
  padding: 14px 16px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.main-area {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-expanded .main-area {
  margin-left: @sidebar-w;
}

.sidebar-collapsed .main-area {
  margin-left: @sidebar-collapsed-w;
}

.topbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #ebeef3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e8eaef;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  transition: all 0.2s;
  font-size: 18px;

  &:hover {
    border-color: @gold;
    color: @gold;
    background: rgba(201, 169, 98, 0.06);
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #909399;

  .breadcrumb-current {
    color: #303133;
    font-weight: 600;
  }

  i { font-size: 12px; }
}

.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover { background: #f5f6f8; }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, @gold, #a88b4a);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
  }

  .user-name {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }

  i { color: #c0c4cc; font-size: 12px; }
}

.main-content {
  flex: 1;
  padding: 20px 24px 32px;
}

/* ========== 侧边栏菜单深度样式 ========== */
/deep/ .sidebar-menu {
  border-right: none !important;
  padding: 8px 0;

  .el-menu-item,
  .el-submenu__title {
    height: 46px !important;
    line-height: 46px !important;
    margin: 2px 10px;
    border-radius: 8px;
    transition: all 0.2s;

    i { color: rgba(255, 255, 255, 0.55); margin-right: 8px; }
    &:hover {
      background: rgba(255, 255, 255, 0.06) !important;
      i { color: @gold-light; }
    }
  }

  .el-menu-item.is-active {
    background: linear-gradient(90deg, rgba(201, 169, 98, 0.22), rgba(201, 169, 98, 0.06)) !important;
    color: @gold-light !important;
    font-weight: 500;
    position: relative;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 10px;
      bottom: 10px;
      width: 3px;
      background: @gold;
      border-radius: 0 2px 2px 0;
    }

    i { color: @gold-light !important; }
  }

  .el-submenu.is-active > .el-submenu__title {
    color: @gold-light !important;
    i { color: @gold-light !important; }
  }

  .el-submenu .el-menu-item {
    min-width: auto;
    padding-left: 48px !important;
    height: 42px !important;
    line-height: 42px !important;
  }

  &.el-menu--collapse {
    .el-menu-item,
    .el-submenu__title {
      margin: 2px 8px;
      padding: 0 !important;
      text-align: center;
    }
  }
}

/deep/ .sidebar-scroll .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>
