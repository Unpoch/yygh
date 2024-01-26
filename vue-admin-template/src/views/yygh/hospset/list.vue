<template>
    <div class="app-container">

        <!--查询表单-->
        <el-form :inline="true" class="demo-form-inline">
            <el-form-item>
                <!-- 后端的查询条件是用HospitalSetQueryVo来接收的,它有两个属性：hosname,hoscode -->
                <el-input v-model="searchObj.hosname" placeholder="医院名称"/>
            </el-form-item>

            <el-form-item>
                <el-input v-model="searchObj.hoscode" placeholder="医院编号"/>
            </el-form-item>

            <el-button type="primary" icon="el-icon-search" @click="fetchData()">查询</el-button>
            <el-button type="default" @click="resetData()">清空</el-button>
        </el-form>

        
        <el-table
          v-loading="listLoading"
          :data="list"
          element-loading-text="数据加载中"
          border
          fit
          highlight-current-row
          @selection-change="handleSelectionChange"> <!-- 复选框发生变化的时候触发这个方法-->
            <!-- 复选框 -->
            <el-table-column type="selection" width="55"/>
            <el-table-column
                            label="序号"
                            width="70"
                            align="center">
                <template slot-scope="scope">
                    {{ (page - 1) * limit + scope.$index + 1 }} <!--序号的计算-->
                </template>
            </el-table-column>

            <el-table-column prop="hosname" label="医院名称" width="180" />

            <el-table-column prop="hoscode" label="医院编号" width="120" />

            <el-table-column prop="apiUrl" label="地址" width="240"/>

            <el-table-column prop="contactsName" label="联系人" width="180"/>

            <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                    {{ scope.row.status===1?'可用':'不可用' }} <!-- scope.row就是当前行数据-->
                </template>
            </el-table-column>

            <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                    <!-- router-link,那么点击button时就会请求：'/yygh/hospset/edit/'+scope.row.id
                        scope.row.id是当前行数据的id
                    那么我们在 src/router/index.js 加上该路由 -->
                    <router-link :to="'/yygh/hospset/edit/'+scope.row.id"> 
                        <el-button type="primary" size="mini" icon="el-icon-edit">修改</el-button>
                    </router-link>
                    <el-button type="danger" size="mini" icon="el-icon-delete" @click="removeDataById(scope.row.id)">删除</el-button>
                    <el-button v-if="scope.row.status==1" type="warning" size="mini" 
                        icon="el-icon-delete" @click="lockHostSet(scope.row.id,0)">锁定</el-button>
                    <el-button v-if="scope.row.status==0" type="warning" size="mini" 
                        icon="el-icon-delete" @click="lockHostSet(scope.row.id,1)">取消锁定</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-button type="danger" size="mini" @click="removeRows()">批量删除</el-button>

        <!-- 分页 -->
        <el-pagination
                    :current-page="page"
                    :page-size="limit"
                    :total="total"
                    style="padding: 30px 0; text-align: center;"
                    layout="total, prev, pager, next, jumper"
                    @current-change="fetchData"
                    />
                    <!-- 当前页发生改变的时候，@current-change，调用fetchData方法，默认传入当前页 -->
    </div>
</template>

<script>
    import hospset from '@/api/yygh/hospset.js'
    export default {
        data() {    //定义数据
            return {
                //变量:值
                listLoading: true, // 是否显示loading信息
                list: null, // 数据列表
                total: 0, // 总记录数
                page: 1, // 页码
                limit: 10, // 每页记录数
                searchObj: {},// 查询条件
                multipleSelection: [] // 批量选择中选择的记录列表
            }
        },
        methods:{
            //page=1是表示，不给该方法传递数据时，page=1，即默认查询第一页;传递page参数时，就使用page参数,不使用默认值1
            fetchData(page=1) { // 调用api层获取数据库中的数据
                console.log('加载列表')
                this.page = page
                this.listLoading = true
                //这里的response就代表了后端返回的R对象,这个模板设置的就是这样
                hospset.getPageList(this.page, this.limit, this.searchObj).then(response => {
                // debugger 设置断点调试
                if (response.success === true) {
                    this.list = response.data.rows
                    this.total = response.data.total
                }
                this.listLoading = false
                })
            },
            //清空的方法
            resetData() {
                this.searchObj = {} //将查询条件清空
                this.fetchData() //再次查询
                //es6支持换行表示语句的结束，因此不需要加逗号分号
            },
            //删除的方法
            removeDataById(id) {
                // debugger
                // console.log(memberId)
                this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    return hospset.removeById(id) //调用请求后端删除的方法
                }).then(() => {
                    this.fetchData() //删除成功后，重新查询，重新显示数据
                    this.$message({
                        type: 'success',
                        message: '删除成功!'
                    })
                }).catch((response) => { // 失败
                    if (response === 'cancel') {
                        this.$message({
                            type: 'info',
                            message: '已取消删除'
                        })
                    } else {
                        this.$message({
                            type: 'error',
                            message: '删除失败'
                        })
                    }
                })
            },
            //当表格复选框发生变化的时候触发
            //selection参数 是选中的所有行数据 (选了多少行就传过来多少行)
            handleSelectionChange(selection) {
                this.multipleSelection = selection;
            },
            //批量删除的方法
            removeRows() {
                this.$confirm('此操作将永久删除医院是设置信息, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => { //确定执行then方法
                    var idList = []
                    //遍历数组得到每个id值，设置到idList里面
                    for(var i=0;i<this.multipleSelection.length;i++) {
                        var obj = this.multipleSelection[i] 
                        var id = obj.id
                        idList.push(id)
                    }
                    //调用接口
                    hospset.removeRows(idList).then(response => {
                        //提示
                        this.$message({
                        type: 'success',
                        message: '删除成功!'
                        })
                        //刷新页面
                        this.fetchData()
                    })
                })
            },
            //医院锁定和解锁的方法
            lockHostSet(id,status) {
                hospset.lockHospSet(id,status).then(response=>{
                    //刷新
                    this.fetchData()
                })
            }
        },
        created() { //vue 中的钩子函数，在数据渲染页面之前调用，我们这里就请求后端获取数据
            this.fetchData() //this ,表示当前vue对象
        }
    }
</script>