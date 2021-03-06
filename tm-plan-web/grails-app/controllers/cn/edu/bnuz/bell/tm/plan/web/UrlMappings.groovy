package cn.edu.bnuz.bell.tm.plan.web

class UrlMappings {

    static mappings = {

        "/checkers"(resources: 'checker', 'includes': []) {
            "/visions"(resources: 'visionCheck', includes: ['index'])
            "/schemes"(resources: 'schemeCheck', includes: ['index'])
        }

        "/approvers"(resources: 'approver', 'includes': []) {
            "/visions"(resources: 'visionApproval', includes: ['index'])
            "/schemes"(resources: 'schemeApproval', includes: ['index'])
        }

        "/reviewers"(resources: 'reviewer', includes: []) {
            "/visions"(resources:'visionReview', includes: []) {
                "/workitems"(resources: 'visionReview', includes: ['show'])
            }
            "/schemes"(resources: 'schemeReview', includes: []) {
                "/workitems"(resources: 'schemeReview', includes: ['show'])
            }
        }

        "/departments"(resources: 'department', includes: []) {
            "/visions"(resources: 'visionDepartment', includes: ['index'])
            "/schemes"(resources: 'schemeDepartment', includes: ['index'])
        }

        "/users"(resources: 'user', includes: []) {
            "/visions"(resources: 'visionDraft', includes: ['index'])
            "/schemes"(resources: 'schemeDraft', includes: ['index'])
        }

        "/visions"(resources:'visionPublic', includes: ['index', 'show'])
        "/schemes"(resources: 'schemePublic', includes: ['index', 'show'])

        group "/admin", {
            "/visions"(resources:'visionAdmin', includes: ['index'])
            "/schemes"(resources: 'schemeAdmin', includes: ['index'])
        }

        group "/settings", {
            "/subjects"(controller: "subjectSettings")
            "/programs"(controller: "programSettings")
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
