<template>
  <div class="app-container">
    <el-form label-width="120px">
      <el-form-item label="医院名称">
        <el-input v-model="hospset.hosname"/>
      </el-form-item>
      <el-form-item label="医院编号">
        <el-input v-model="hospset.hoscode"/>
      </el-form-item>
      <el-form-item label="api地址">
        <el-input v-model="hospset.apiUrl"/>
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="hospset.contactsName"/>
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="hospset.contactsPhone"/>
      </el-form-item>
      <el-form-item>
        <el-button :disabled="saveBtnDisabled" type="primary" @click="saveOrUpdate">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>


<script>
    import hospset from '@/api/yygh/hospset.js';

    export default {
        data() {
          return {
            hospset: {}, //与表单进行了数据的双向绑定
            saveBtnDisabled: false // 保存按钮是否禁用,true表示不可点击
          }
        },
        methods:{
          saveOrUpdate() {
            this.saveBtnDisabled = true
            if (!this.hospset.id) {
                this.saveData()
            } else {//如果携带了id，表明是修改，调用修改的方法
                this.updateData()
            }
          },
          //保存
          saveData() {
            hospset.save(this.hospset).then(response=>{
              this.$message({
                type: 'success',
                message: '保存成功'
              })
            })
            //通过路由的方式进行页面跳转，写法固定this.$router
            this.$router.push({ path: '/yygh/hospset/list' }) 
          },
          //根据id查询记录
          fetchDataById(id) {
                hospset.getById(id).then(response=>{
                    this.hospset=response.data.item //数据回显
                }).catch((response) => {
                    this.$message({
                        type: 'error',
                        message: '获取数据失败'
                    })
                })                  
          },
          //更新记录
          updateData() {
              this.saveBtnDisabled = true
              hospset.updateById(this.hospset).then(response=>{
              return this.$message({
                  type: 'success',
                  message: '修改成功!'
              })}).then(resposne => {
                  this.$router.push({ path: '/yygh/hospset/list' }) //跳转页面
              }).catch((response) => {
                  // console.log(response)
                  this.$message({
                      type: 'error',
                      message: '保存失败'
                  })
              })
          }
        },
        created() {
           //点击修改之后，在修改页面出现之前，要将数据查询出来 渲染到页面,因此定义在钩子函数中
           //this.$route.params 获取路由的参数params
          if (this.$route.params && this.$route.params.id) {
              const id = this.$route.params.id
              this.fetchDataById(id) //调用根据id查询记录的方法
          }
        }
    }
</script>