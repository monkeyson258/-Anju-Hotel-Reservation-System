<template>
  <div class="front-layout">
    <header class="site-header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/front')">
          <img src="@/assets/logo.png" class="brand-logo" alt="logo" />
          <div class="brand-info">
            <span class="brand-name">{{ $sysTitle }}</span>
            <span class="brand-tagline">尊享舒适旅居</span>
          </div>
        </div>

        <nav class="main-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >{{ item.label }}</router-link>
        </nav>

        <div class="header-actions">
          <template v-if="!token">
            <router-link to="/login" class="btn-ghost">登录</router-link>
            <router-link to="/register" class="btn-gold">注册</router-link>
          </template>
          <el-dropdown v-else trigger="click" @command="handleCommand">
            <div class="user-pill">
              <el-avatar v-if="head" :size="32" :src="$baseFileUrl + head"></el-avatar>
              <img v-else src="@/assets/head.jpg" class="user-avatar-img" />
              <span>{{ user?.name }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="userInfo" icon="el-icon-user">个人信息</el-dropdown-item>
              <el-dropdown-item command="appointment" icon="el-icon-date">我的预约</el-dropdown-item>
              <el-dropdown-item command="orders" icon="el-icon-s-order">我的入住</el-dropdown-item>
              <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="front-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <span class="footer-brand">{{ $sysTitle }}</span>
        <span class="footer-copy">Copyright © {{ year }} All Rights Reserved</span>
      </div>
    </footer>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { removeToken } from '@/utils/auth'

export default {
  name: 'FrontLayout',
  data() {
    return {
      head: '',
      year: '',
      navItems: [
        { path: '/front', label: '首页' },
        { path: '/front/room', label: '预约入住' },
        { path: '/front/notice', label: '系统公告' },
        { path: '/front/message', label: '用户留言' }
      ]
    }
  },
  computed: {
    ...mapState(['user', 'role', 'token'])
  },
  mounted() {
    this.year = new Date().getFullYear()
    this.head = this.user?.head
  },
  methods: {
    ...mapMutations(['setUser', 'setRole', 'setToken']),
    isActive(path) {
      if (path === '/front') {
        return this.$route.path === '/front' || this.$route.path === '/front/'
      }
      return this.$route.path.startsWith(path)
    },
    handleCommand(cmd) {
      const routes = {
        userInfo: '/front/userInfo',
        appointment: '/front/appointment',
        orders: '/front/orders'
      }
      if (cmd === 'logout') {
        this.logout()
      } else if (routes[cmd]) {
        this.$router.push(routes[cmd])
      }
    },
    logout() {
      this.$confirm('确定退出吗?', '温馨提示', {
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
@gold: #c9a962;
@dark: #141820;

.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6f8;
}

.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(20, 24, 32, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(201, 169, 98, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;

  .brand-logo {
    width: 38px;
    height: 38px;
    object-fit: contain;
  }

  .brand-info {
    display: flex;
    flex-direction: column;
  }

  .brand-name {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.5px;
    line-height: 1.2;
  }

  .brand-tagline {
    font-size: 11px;
    color: @gold;
    letter-spacing: 2px;
    margin-top: 2px;
  }
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  padding: 8px 18px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.72);
  border-radius: 6px;
  transition: all 0.2s;
  text-decoration: none;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.08);
  }

  &.active {
    color: @gold;
    background: rgba(201, 169, 98, 0.12);
    font-weight: 500;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.btn-ghost {
  padding: 7px 18px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 6px;
  transition: all 0.2s;
  text-decoration: none;

  &:hover {
    border-color: @gold;
    color: @gold;
  }
}

.btn-gold {
  padding: 7px 18px;
  font-size: 14px;
  color: @dark;
  background: linear-gradient(135deg, @gold, #a88b4a);
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s;
  text-decoration: none;

  &:hover {
    box-shadow: 0 4px 12px rgba(201, 169, 98, 0.4);
    transform: translateY(-1px);
  }
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.2s;
  color: #fff;
  font-size: 14px;

  &:hover { background: rgba(255, 255, 255, 0.14); }

  .user-avatar-img {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
  }

  i { font-size: 12px; color: rgba(255, 255, 255, 0.5); }
}

.front-main {
  flex: 1;
  padding-top: 64px;
  min-height: calc(100vh - 64px);
}

.site-footer {
  background: @dark;
  border-top: 1px solid rgba(201, 169, 98, 0.12);
  padding: 24px;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.footer-brand {
  font-size: 14px;
  color: @gold;
  font-weight: 500;
  letter-spacing: 1px;
}

.footer-copy {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
</style>
