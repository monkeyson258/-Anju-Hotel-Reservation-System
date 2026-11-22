<template>
    <div class="auth-page">
        <div class="auth-bg"></div>
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <img src="@/assets/logo.png" class="auth-logo" alt="logo" />
                <h2 class="title" @click="$router.push('/front')">{{ $sysTitle }}</h2>
                <p class="subtitle">创建账户 · 开启尊享之旅</p>
            </div>
            <el-form :model="form" label-position="top" class="auth-form">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="账号">
                            <el-input placeholder="请输入账号" v-model="form.username" prefix-icon="el-icon-user"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="密码">
                            <el-input type="password" placeholder="请输入密码" v-model="form.password" prefix-icon="el-icon-lock" show-password></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="姓名">
                            <el-input placeholder="请输入姓名" v-model="form.name"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="性别">
                            <el-select v-model="form.gender" placeholder="请选择" style="width:100%">
                                <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机">
                            <el-input placeholder="请输入手机" v-model="form.phone"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="身份证">
                            <el-input placeholder="请输入身份证" v-model="form.idcard"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="头像">
                            <el-upload name="avatar" class="upload-demo" action="#" :http-request="uploadHeadFile"
                                :file-list="fileList" list-type="picture">
                                <el-button size="small" type="primary">点击上传</el-button>
                            </el-upload>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item>
                    <el-button type="primary" class="submit-btn" @click="submitForm">注 册</el-button>
                    <div class="auth-footer">
                        已有账号？<router-link to="/login">立即登录</router-link>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script>
import { registerAPI } from '@/api/system'
import request from '@/utils/request'
export default {
    name: 'Register',
    data() {
        return {
            form: { gender: 1 },
            genderOptions: [
                { label: '男', value: 1 },
                { label: '女', value: 2 }
            ],
            fileList: []
        }
    },
    methods: {
        async submitForm() {
            const res = await registerAPI(this.form)
            this.$message({
                message: res.message,
                type: res.flag ? "success" : "error",
            });
            setTimeout(() => {
                this.$router.push('/login')
            }, 1000);
        },
        uploadHeadFile(item) {
            let fileObj = item.file;
            const form = new FormData();
            form.append("photo", fileObj);
            request.post("/file/uploadPhoto", form, "form").then((resp) => {
                this.form.head = resp.data.filename
                this.fileList = [{
                    name: this.form.head,
                    url: this.$baseFileUrl + this.form.head
                }]
            })
        },
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
    padding: 40px 24px;
}

.auth-bg {
    position: fixed;
    inset: 0;
    background: linear-gradient(135deg, #141820 0%, #1c2333 40%, #2a3142 100%);
    z-index: 0;

    &::after {
        content: "";
        position: absolute;
        inset: 0;
        background: radial-gradient(ellipse at 70% 20%, rgba(201, 169, 98, 0.12) 0%, transparent 60%);
    }
}

.auth-card {
    position: relative;
    z-index: 1;
    width: 420px;
    max-width: 100%;
    padding: 36px 40px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.auth-card-wide {
    width: 560px;
}

.auth-header {
    text-align: center;
    margin-bottom: 24px;

    .auth-logo { width: 44px; height: 44px; margin-bottom: 10px; }
    .title {
        font-size: 20px;
        font-weight: 600;
        color: #141820;
        margin: 0 0 4px;
        cursor: pointer;
        &:hover { color: #c9a962; }
    }
    .subtitle { margin: 0; font-size: 13px; color: #909399; }
}

.auth-form /deep/ .el-form-item__label {
    font-weight: 500;
    color: #606266;
    padding-bottom: 2px;
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
    margin-top: 14px;
    font-size: 13px;
    color: #909399;
}
</style>
