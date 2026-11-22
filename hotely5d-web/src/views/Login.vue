<template>
    <div class="auth-page">
        <div class="auth-bg"></div>
        <div class="auth-card">
            <div class="auth-header">
                <img src="@/assets/logo.png" class="auth-logo" alt="logo" />
                <h2 class="title" @click="$router.push('/front')">{{ $sysTitle }}</h2>
                <p class="subtitle">欢迎登录 · 尊享品质旅居</p>
            </div>
            <el-form :model="loginForm" label-position="top" class="auth-form">
                <el-form-item label="账号">
                    <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="请输入账户"></el-input>
                </el-form-item>
                <el-form-item label="密码">
                    <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" type="password" placeholder="请输入密码" show-password></el-input>
                </el-form-item>
                <el-form-item label="登录身份">
                    <el-radio-group v-model="loginForm.role" class="role-group">
                        <el-radio :label="1">管理员</el-radio>
                        <el-radio :label="2">用户</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" class="submit-btn" @click="login">登 录</el-button>
                    <div class="auth-footer">
                        没有账号？<router-link to="/register">立即注册</router-link>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script>
import { loginAPI, getUserInfoByTokenAPI } from '@/api/system'
import { mapMutations } from 'vuex'
import { setToken } from '@/utils/auth'
export default {
    name: 'Login',
    data() {
        return {
            loginForm: {
                role: 1,
                username: '',
                password: "1"
            }
        }
    },
    methods: {
        ...mapMutations(['setUser','setRole','setToken']),
        async login() {
            const res = await loginAPI(this.loginForm)
            this.$message({
                type: res.flag ? 'success' : 'error',
                message: res.message
            })
            if (res.flag) {
                setToken(res.data)
                this.setToken(res.data)
                const result = await getUserInfoByTokenAPI()
                this.setUser(result.data)
                this.setRole(this.loginForm.role)
                this.$router.replace(this.loginForm.role == 1?"/admin":"/front")
            }
        }
    }
}
</script>

<style lang="less" scoped>
.auth-page {
    width: 100vw;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
}

.auth-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #141820 0%, #1c2333 40%, #2a3142 100%);

    &::after {
        content: "";
        position: absolute;
        inset: 0;
        background: radial-gradient(ellipse at 70% 20%, rgba(201, 169, 98, 0.12) 0%, transparent 60%),
                    radial-gradient(ellipse at 20% 80%, rgba(201, 169, 98, 0.08) 0%, transparent 50%);
    }
}

.auth-card {
    position: relative;
    z-index: 1;
    width: 420px;
    max-width: calc(100vw - 48px);
    padding: 40px 44px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.auth-header {
    text-align: center;
    margin-bottom: 32px;

    .auth-logo {
        width: 48px;
        height: 48px;
        margin-bottom: 12px;
    }

    .title {
        font-size: 22px;
        font-weight: 600;
        color: #141820;
        margin: 0 0 6px;
        cursor: pointer;
        transition: color 0.2s;
        &:hover { color: #c9a962; }
    }

    .subtitle {
        margin: 0;
        font-size: 13px;
        color: #909399;
        letter-spacing: 1px;
    }
}

.auth-form {
    /deep/ .el-form-item__label {
        font-weight: 500;
        color: #606266;
        padding-bottom: 4px;
    }
}

.role-group {
    width: 100%;
}

.submit-btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    letter-spacing: 4px;
    border-radius: 8px;
}

.auth-footer {
    text-align: center;
    margin-top: 16px;
    font-size: 13px;
    color: #909399;
}
</style>
