<style lang="less">
    @import './login.less';
</style>

<template>
    <div class="login" @keydown.enter="handleSubmit">
        <div class="login-con">
            <Card :bordered="false">
                <p slot="title">
                    <Icon type="log-in"></Icon>
                    欢迎登录
                </p>
                <div class="form-con">
                    <Form ref="loginForm" :model="form" :rules="rules">
                        <FormItem prop="userName">
                            <Input v-model="form.userName" placeholder="请输入用户名">
                                <span slot="prepend">
                                    <Icon :size="16" type="person"></Icon>
                                </span>
                            </Input>
                        </FormItem>
                        <FormItem prop="password">
                            <Input type="password" v-model="form.password" placeholder="请输入密码">
                                <span slot="prepend">
                                    <Icon :size="14" type="locked"></Icon>
                                </span>
                            </Input>
                        </FormItem>
                        <FormItem>
                            <Button @click="handleSubmit" type="primary" long>登录</Button>
                        </FormItem>
                    </Form>
                    <p class="login-tip">输入用户名和密码即可</p>
                </div>
            </Card>
        </div>
        <Spin size="large" fix v-if="spinShow"></Spin>
    </div>
</template>

<script>
import Cookies from 'js-cookie';
export default {
    data () {
        return {
            spinShow:false,
            form: {
                userName: '',
                password: ''
            },
            rules: {
                userName: [
                    { required: true, message: '账号不能为空', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: '密码不能为空', trigger: 'blur' }
                ]
            }
        };
    },
    methods: {
        handleSubmit () {
            let vm=this;
            this.$refs.loginForm.validate((valid) => {
                if (valid) {

                    this.spinShow=true;
                    this.$store.commit('setAvator', 'http://p2qdi4xy4.bkt.clouddn.com/head.png');
                    this.$store.dispatch('user_login',{user:this.form.userName,password:this.form.password}).then((res)=>{
                        vm.spinShow=false;
                        if(res.resCode&&res.resCode=='success'){

                            let resData=JSON.parse(res.resData);
                            let menuArray=[];//登录用户持有的菜单权限
                            let serviceArray=[];//登录用户持有的服务权限
                            for(let i=0;i<resData.resList.length;i++){
                                let tmp=resData.resList[i];
                                if(tmp.type=='0'&&tmp.enabled=='y'){
                                    menuArray.push(tmp.url);
                                if(tmp.children.length>0){
                                    for(let j=0;j<tmp.children.length;j++){
                                        if(tmp.children[j].type=='0'&&tmp.children[j].enabled=='y'){
                                            menuArray.push(tmp.children[j].url);
                                        }

                                    }
                                }
                                }if(tmp.type=='1'&&tmp.enabled=='y'){
                                    serviceArray.push(tmp.url);
                                    if(tmp.children.length>0){
                                        for(let j=0;j<tmp.children.length;j++){
                                            if(tmp.children[j].type=='0'&&tmp.children[j].enabled=='y'){
                                                serviceArray.push(tmp.children[j].url);
                                            }

                                        }
                                    }
                                }
                            }
//                            console.info(menuArray)
                            Cookies.set('menuArray',menuArray);
                            Cookies.set('serviceArray',serviceArray);
                            Cookies.set('user',resData.username);
                            Cookies.set('loginname',resData.loginname);
                            this.$router.push({
                                name: 'home_index'
                            });
                        }else if(res.resCode&&res.resCode=='fail'){

                        }
                    }).catch(()=>{
                        vm.spinShow=false;
                    })

//                    if (this.form.userName === 'iview_admin') {
//                        Cookies.set('access', 0);
//                    } else {
//                        Cookies.set('access', 1);
//                    }

                }
            });
        }
    }
};
</script>

<style>

</style>
